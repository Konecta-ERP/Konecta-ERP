import os
import uuid
import shutil
import logging
from pathlib import Path
from typing import List, Dict, Optional

from fastapi import FastAPI, UploadFile, File, HTTPException, Query
from pydantic import BaseModel
from dotenv import load_dotenv

from azure.ai.formrecognizer import DocumentAnalysisClient
from azure.core.credentials import AzureKeyCredential

from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# -----------------------------
# 1) Load environment variables
# -----------------------------
load_dotenv()

AZURE_ENDPOINT = os.getenv("AZURE_FORMRECOGNIZER_ENDPOINT")
AZURE_KEY = os.getenv("AZURE_FORMRECOGNIZER_KEY")

if not AZURE_ENDPOINT or not AZURE_KEY:
    raise RuntimeError(
        "Missing AZURE_FORMRECOGNIZER_ENDPOINT or AZURE_FORMRECOGNIZER_KEY in .env"
    )

document_client = DocumentAnalysisClient(
    endpoint=AZURE_ENDPOINT,
    credential=AzureKeyCredential(AZURE_KEY)
)

UPLOAD_DIR = Path("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)

# -----------------------------
# 2) Pydantic models (schema)
# -----------------------------
class LineItem(BaseModel):
    description: Optional[str] = None
    quantity: Optional[float] = None
    unit_price: Optional[float] = None
    line_total: Optional[float] = None


class InvoiceExtracted(BaseModel):
    document_type: str = "invoice"
    supplier_name: Optional[str] = None
    supplier_tax_id: Optional[str] = None
    invoice_number: Optional[str] = None
    invoice_date: Optional[str] = None
    due_date: Optional[str] = None
    total_amount: Optional[float] = None
    currency: Optional[str] = None
    line_items: List[LineItem] = []
    field_confidences: Dict[str, float] = {}


class ValidationResult(BaseModel):
    status: str  # "VALID" or "REVIEW_REQUIRED"
    errors: List[str] = []


class ProcessedInvoiceResponse(BaseModel):
    extracted_data: InvoiceExtracted
    validation: ValidationResult


# -----------------------------
# 3) Helper: map Azure result -> our schema
# -----------------------------
def _get_field(doc, name: str):
    """
    Safely retrieve a field value and confidence from an Azure AnalyzedDocument.
    Returns (value, confidence) or (None, 0.0)
    """
    field = doc.fields.get(name)
    logger.info(f" Ana Henaaa Field '{name}': {field}")
    if not field:
        return None, 0.0

    # For dates, the value might be a date object; we convert to string
    value = getattr(field, "value", None)
    if value is None:
        value = field.content

    try:
        if hasattr(value, "isoformat"):
            value = value.isoformat()
    except Exception:
        pass

    conf = getattr(field, "confidence", 0.0)
    return value, conf

def parse_number(value):
    """
    Try to convert different number formats like:
    '381,12 €', '1 234,56', '1,234.56', '$4,36' into a float.
    """
    if value is None:
        return None

    # Already a number
    if isinstance(value, (int, float)):
        return float(value)

    if not isinstance(value, str):
        # If it's an object with 'amount' attribute (Azure CurrencyValue)
        if hasattr(value, 'amount'):
            return float(value.amount)
        # Unknown type, log and return None
        logger.warning(f"Unknown value type for parsing: {type(value)}")
        return None

    s = value.strip()
    
    # Remove currency symbols and spaces
    for ch in ["€", "$", "£", "¥", "₹"]:
        s = s.replace(ch, "")
    s = s.replace(" ", "")

    if not s:
        return None
    
    # Now handle separators
    # Case 1: European format '381,12' or '4,36'
    if s.count(",") == 1 and s.count(".") == 0:
        s = s.replace(",", ".")
    # Case 2: '1.234,56' → remove thousands '.' then convert ',' to '.'
    elif s.count(",") == 1 and s.count(".") >= 1:
        s = s.replace(".", "").replace(",", ".")
    # Case 3: '1,234.56' → remove thousands ',' keep '.'
    elif s.count(".") == 1 and s.count(",") >= 1:
        s = s.replace(",", "")

    try:
        return float(s)
    except Exception as e:
        logger.warning(f"Failed to parse number '{value}': {e}")
        return None


def map_invoice_from_azure(result) -> InvoiceExtracted:
    """
    Map Azure prebuilt-invoice AnalyzeResult to our InvoiceExtracted schema.
    """
    if not result.documents:
        # No documents found, return empty
        logger.warning("No documents found in Azure result")
        return InvoiceExtracted()

    doc = result.documents[0]

    # Log all available fields for debugging
    logger.info(f"Available fields: {list(doc.fields.keys())}")

    supplier_name, conf_supplier_name = _get_field(doc, "VendorName")
    supplier_tax_id, conf_supplier_tax = _get_field(doc, "VendorTaxId")
    
    # Log vendor address for debugging
    vendor_addr, _ = _get_field(doc, "VendorAddress")
    logger.info(f"VendorAddress: {vendor_addr}")
    
    # If VendorTaxId is not found, try multiple strategies
    if not supplier_tax_id:
        # Strategy 1: Check VendorAddress field
        if vendor_addr:
            vendor_addr_str = str(vendor_addr)
            if "Tax Id:" in vendor_addr_str:
                parts = vendor_addr_str.split("Tax Id:")
                if len(parts) > 1:
                    supplier_tax_id = parts[1].strip().split("\n")[0].strip()
                    conf_supplier_tax = 0.8
                    logger.info(f"Extracted tax ID from VendorAddress: {supplier_tax_id}")
        
        # Strategy 2: Search through all OCR content near vendor section
        if not supplier_tax_id:
            try:
                # Get all text content from the document
                for page in result.pages:
                    page_text = ""
                    if hasattr(page, 'lines'):
                        for line in page.lines:
                            page_text += line.content + "\n"
                    
                    # Look for Tax Id pattern in the text
                    if "Tax Id:" in page_text:
                        import re
                        # Match "Tax Id: XXX-XX-XXXX" pattern
                        match = re.search(r'Tax Id:\s*([\d-]+)', page_text)
                        if match:
                            supplier_tax_id = match.group(1).strip()
                            conf_supplier_tax = 0.75
                            logger.info(f"Extracted tax ID from OCR content: {supplier_tax_id}")
                            break
            except Exception as e:
                logger.warning(f"Failed to extract tax ID from OCR content: {e}")
    
    invoice_number, conf_invoice_number = _get_field(doc, "InvoiceId")
    invoice_date, conf_invoice_date = _get_field(doc, "InvoiceDate")
    due_date, conf_due_date = _get_field(doc, "DueDate")
    
    # Try multiple field names for total
    total_amount, conf_total_amount = _get_field(doc, "InvoiceTotal")
    if total_amount is None:
        total_amount, conf_total_amount = _get_field(doc, "AmountDue")
    
    # Try to extract currency code
    currency_code, conf_currency = _get_field(doc, "CurrencyCode")
    
    # Log raw values before parsing
    logger.info(f"Raw total_amount: {total_amount} (type: {type(total_amount)})")
    logger.info(f"Raw currency: {currency_code}")

    # Line items - with better error logging
    line_items: List[LineItem] = []
    items_field = doc.fields.get("Items")
    if items_field and items_field.value:
        logger.info(f"Found {len(items_field.value)} line items")
        for idx, item in enumerate(items_field.value):
            try:
                item_fields = item.value
                logger.info(f"Line item {idx} fields: {list(item_fields.keys())}")
                
                desc_field = item_fields.get("Description")
                qty_field = item_fields.get("Quantity")
                unit_price_field = item_fields.get("UnitPrice")
                amount_field = item_fields.get("Amount")

                description = None
                if desc_field:
                    description = desc_field.value or desc_field.content

                # Parse quantities and prices
                quantity = parse_number(qty_field.value) if qty_field and qty_field.value else None
                unit_price = parse_number(unit_price_field.value) if unit_price_field and unit_price_field.value else None
                line_total = parse_number(amount_field.value) if amount_field and amount_field.value else None

                logger.info(f"Line item {idx}: desc={description}, qty={quantity}, price={unit_price}, total={line_total}")

                line_items.append(
                    LineItem(
                        description=description,
                        quantity=quantity,
                        unit_price=unit_price,
                        line_total=line_total,
                    )
                )
            except Exception as e:
                # Log the error but continue
                logger.error(f"Error parsing line item {idx}: {e}", exc_info=True)
                continue
    else:
        logger.warning("No line items found in Azure result")

    # Build confidence dict
    field_confidences = {
        "supplier_name": float(conf_supplier_name),
        "supplier_tax_id": float(conf_supplier_tax),
        "invoice_number": float(conf_invoice_number),
        "invoice_date": float(conf_invoice_date),
        "due_date": float(conf_due_date),
        "total_amount": float(conf_total_amount),
        "currency": float(conf_currency),
    }

    # Parse total amount and extract currency
    parsed_total = parse_number(total_amount)
    logger.info(f"Parsed total_amount: {parsed_total}")
    
    # Extract currency from CurrencyValue object or string
    extracted_currency = currency_code
    if extracted_currency is None and hasattr(total_amount, 'code'):
        # Azure CurrencyValue object has a 'code' attribute
        extracted_currency = total_amount.code
        logger.info(f"Extracted currency from CurrencyValue.code: {extracted_currency}")
    elif extracted_currency is None and hasattr(total_amount, 'symbol'):
        # Some versions might have a symbol attribute
        extracted_currency = total_amount.symbol
    elif extracted_currency is None and isinstance(total_amount, str):
        # Fallback: extract from string
        for symbol in ["$", "€", "£", "¥", "₹"]:
            if symbol in total_amount:
                extracted_currency = symbol
                break
    
    logger.info(f"Final currency: {extracted_currency}")

    return InvoiceExtracted(
        supplier_name=supplier_name,
        supplier_tax_id=supplier_tax_id,
        invoice_number=str(invoice_number) if invoice_number is not None else None,
        invoice_date=str(invoice_date) if invoice_date is not None else None,
        due_date=str(due_date) if due_date is not None else None,
        total_amount=parsed_total,
        currency=str(extracted_currency) if extracted_currency is not None else None,
        line_items=line_items,
        field_confidences=field_confidences,
    )


# -----------------------------
# 4) Helper: validation logic
# -----------------------------
def validate_invoice(inv: InvoiceExtracted) -> ValidationResult:
    errors: List[str] = []

    # Required fields
    if not inv.supplier_name:
        errors.append("Missing supplier_name")
    if not inv.invoice_number:
        errors.append("Missing invoice_number")
    if not inv.invoice_date:
        errors.append("Missing invoice_date")
    if inv.total_amount is None:
        errors.append("Missing total_amount")

    # Line items vs total
    if inv.total_amount is not None and inv.line_items:
        sum_lines = sum(li.line_total or 0 for li in inv.line_items)
        # More lenient tolerance for tax/rounding differences
        tolerance = max(1.0, inv.total_amount * 0.05)  # 5% or 1 currency unit, whichever is larger
        if abs(sum_lines - inv.total_amount) > tolerance:
            errors.append(
                f"Line items total ({sum_lines:.2f}) does not match invoice total ({inv.total_amount:.2f}). "
                f"Difference: {abs(sum_lines - inv.total_amount):.2f}"
            )

    # Confidence threshold for critical fields
    crit_fields = ["supplier_name", "invoice_number", "invoice_date", "total_amount"]
    for f in crit_fields:
        conf = inv.field_confidences.get(f, 0.0)
        if conf < 0.7:
            errors.append(f"Low confidence for field '{f}' ({conf:.2f})")

    status = "VALID" if not errors else "REVIEW_REQUIRED"
    return ValidationResult(status=status, errors=errors)


# -----------------------------
# 5) FastAPI app definition & eureka
# -----------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    # --- STARTUP: Register with Eureka ---
    print("Starting up and registering with Eureka...")
    await eureka_client.init_async(
        eureka_server="http://discovery-server:8761/eureka",
        app_name="DOCUMENT-PROCESSOR",        # Service Name for Gateway to use
        instance_port=8000,              # The internal container port
        instance_host="document-processor-instance" # Docker container name
    )
    yield
    # --- SHUTDOWN: Cleanup is handled automatically ---

app = FastAPI(
    title="Automated Document Processing Service",
    description="Processes invoices using Azure Form Recognizer prebuilt invoice model.",
    version="1.0.0",
    lifespan=lifespan
)

@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.post(
    "/process-invoice",
    response_model=ProcessedInvoiceResponse,
    summary="Upload an invoice PDF/image and extract structured data",
)
async def process_invoice(
    file: UploadFile = File(...),
    save_file: bool = Query(False, description="If True, keep uploaded file on disk"),
):
    # 1. Save uploaded file temporarily
    suffix = Path(file.filename).suffix or ".pdf"
    temp_name = f"{uuid.uuid4()}{suffix}"
    temp_path = UPLOAD_DIR / temp_name

    try:
        with temp_path.open("wb") as f:
            shutil.copyfileobj(file.file, f)

        logger.info(f"Processing file: {temp_name}")

        # 2. Call Azure Form Recognizer (prebuilt-invoice model)
        with temp_path.open("rb") as f:
            poller = document_client.begin_analyze_document(
                model_id="prebuilt-invoice",
                document=f,
            )
            result = poller.result()

        # 3. Map to our schema
        extracted = map_invoice_from_azure(result)

        # 4. Validate
        validation = validate_invoice(extracted)

        # 5. Optionally delete file
        if not save_file:
            temp_path.unlink(missing_ok=True)

        logger.info(f"Processing complete. Status: {validation.status}")

        return ProcessedInvoiceResponse(
            extracted_data=extracted,
            validation=validation,
        )

    except Exception as e:
        # Clean up temp file if something fails
        logger.error(f"Processing failed: {e}", exc_info=True)
        temp_path.unlink(missing_ok=True)
        raise HTTPException(status_code=500, detail=f"Processing failed: {e}")