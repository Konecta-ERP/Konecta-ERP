package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

@Service
public class PDFExportService {

    public byte[] exportTrialBalance(TrialBalanceReportDTO report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        doc.add(new Paragraph("Trial Balance - " + report.getPeriodLabel(), titleFont));
        doc.add(new Paragraph("Status: " + report.getTbStatus()));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell("Account ID");
        table.addCell("Account Name");
        table.addCell("Type");
        table.addCell("Debit");
        table.addCell("Credit");
        table.addCell("Abnormal?");

        for (TrialBalanceRowDTO row : report.getRows()) {
            table.addCell(row.getAccountId());
            table.addCell(row.getAccountName());
            table.addCell(row.getAccountType().toString());
            table.addCell(row.getDebitBalance() != null ? row.getDebitBalance().toPlainString() : "");
            table.addCell(row.getCreditBalance() != null ? row.getCreditBalance().toPlainString() : "");
            table.addCell(row.isAbnormal() ? "Yes" : "No");
        }

        PdfPCell totalsCell = new PdfPCell(new Phrase("TOTALS"));
        totalsCell.setColspan(3);
        totalsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalsCell);
        table.addCell(report.getTotalDebits().toPlainString());
        table.addCell(report.getTotalCredits().toPlainString());
        table.addCell("");

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    private static final DecimalFormat DEC = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Fonts for PDF
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font DATA_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font BOLD_DATA_FONT = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    private static final Font STATUS_BALANCED_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 153, 0)); // Green
    private static final Font STATUS_UNBALANCED_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED);
    private static final Font SECTION_HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLUE);

    public byte[] exportGl(GLResponseDTO response) {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 72, 72);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("General Ledger", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            // Filters (date and accounts)
            doc.add(new Paragraph("From: " + response.getFromDate() + "    To: " + response.getToDate()));
            if (response.getFilteredAccounts() != null && !response.getFilteredAccounts().isEmpty()) {
                doc.add(new Paragraph("Accounts: " + String.join(", ", response.getFilteredAccounts())));
            }
            doc.add(Chunk.NEWLINE);

            // Table definition (9 columns)
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 1.5f, 1.5f, 4f, 1.8f, 1.8f, 2f});

            // Column headers
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            String[] headers = {"Account ID", "Account Name", "Tx Date", "Tx ID", "Entry ID", "Description", "Debit", "Credit", "Running Bal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(4f);
                table.addCell(cell);
            }

            // Data rows
            java.util.List<GLRowDTO> rows = response.getEntries();
            if (rows != null) {
                for (GLRowDTO r : rows) {
                    PdfPCell cell1 = new PdfPCell(new Phrase(r.getAccountPK() != null ? r.getAccountPK() + "" : ""));
                    PdfPCell cell2 = new PdfPCell(new Phrase(r.getAccountName() != null ? r.getAccountName() : ""));
                    PdfPCell cell3 = new PdfPCell(new Phrase(r.getTransactionDate() != null ? r.getTransactionDate().toString() : ""));
                    PdfPCell cell4 = new PdfPCell(new Phrase(r.getTransactionId() != null ? r.getTransactionId().toString() : ""));
                    PdfPCell cell5 = new PdfPCell(new Phrase(r.getEntryId() != null ? r.getEntryId().toString() : ""));
                    PdfPCell cell6 = new PdfPCell(new Phrase(r.getDescription() != null ? r.getDescription() : ""));
                    PdfPCell cell7 = new PdfPCell(new Phrase(r.getDebitAmount() != null ? DEC.format(r.getDebitAmount()) : ""));
                    PdfPCell cell8 = new PdfPCell(new Phrase(r.getCreditAmount() != null ? DEC.format(r.getCreditAmount()) : ""));
                    PdfPCell cell9 = new PdfPCell(new Phrase(r.getRunningBalance() != null ? DEC.format(r.getRunningBalance()) : ""));

                    // Add all to table
                    table.addCell(cell1);
                    table.addCell(cell2);
                    table.addCell(cell3);
                    table.addCell(cell4);
                    table.addCell(cell5);
                    table.addCell(cell6);
                    table.addCell(cell7);
                    table.addCell(cell8);
                    table.addCell(cell9);
                }
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create PDF", ex);
        }
    }

    public byte[] exportIncomeStatement(IncomeStatementDTO dto) {
        // Use A4 Portrait (unlike the A4.rotate() in the GL example)
        Document doc = new Document(PageSize.A4, 36, 36, 72, 72);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Title
            Paragraph title = new Paragraph("Income Statement (Profit & Loss)", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            // Filters (Period)
            String periodInfo = String.format("Period: %s (%s to %s)",
                    dto.getPeriodLabel(),
                    dto.getStartDate().format(DATE_FMT),
                    dto.getEndDate().format(DATE_FMT));
            Paragraph filters = new Paragraph(periodInfo);
            filters.setAlignment(Element.ALIGN_CENTER);
            doc.add(filters);
            doc.add(Chunk.NEWLINE);

            // Table definition (5 columns)
            // [Line Item, Actual, Budget, Variance, Variance %]
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 2f, 2f, 2f, 1.5f}); // Line item wider

            // Column headers
            addPdfHeaderCell(table, "Line Item");
            addPdfHeaderCell(table, "Actual");
            addPdfHeaderCell(table, "Budget");
            addPdfHeaderCell(table, "Variance");
            addPdfHeaderCell(table, "Variance %");

            // --- Data Rows ---
            // Revenue
            addPdfRow(table, "Revenue", dto.getRevenueActual(), dto.getRevenueBudget(),
                    dto.getRevenueVariance(), dto.getRevenueVariancePct(), DATA_FONT);

            // COGS
            addPdfRow(table, "Cost of Goods Sold (COGS)", dto.getCogsActual(), dto.getCogsBudget(),
                    dto.getCogsVariance(), null, DATA_FONT); // No Pct for COGS in DTO

            // Gross Profit (Bold)
            addPdfSpacerRow(table);
            addPdfRow(table, "Gross Profit", dto.getGrossProfitActual(), dto.getGrossProfitBudget(),
                    dto.getGrossProfitVariance(), dto.getGrossProfitVariancePct(), BOLD_DATA_FONT);
            addPdfSpacerRow(table);

            // OpEx
            addPdfRow(table, "Operating Expenses (OpEx)", dto.getOpexActual(), dto.getOpexBudget(),
                    null, null, DATA_FONT); // No Variance for OpEx in DTO

            // EBIT (Bold)
            addPdfRow(table, "EBIT", dto.getEbitActual(), dto.getEbitBudget(),
                    dto.getEbitVariance(), dto.getEbitVariancePct(), BOLD_DATA_FONT);
            addPdfSpacerRow(table);

            // Other Income/Expense
            addPdfRow(table, "Other Income", dto.getOtherIncomeActual(), dto.getOtherIncomeBudget(),
                    null, null, DATA_FONT);
            addPdfRow(table, "Other Expense", dto.getOtherExpenseActual(), dto.getOtherExpenseBudget(),
                    null, null, DATA_FONT);
            addPdfSpacerRow(table);

            // Net Income (Bold)
            addPdfRow(table, "Net Income", dto.getNetIncomeActual(), dto.getNetIncomeBudget(),
                    dto.getNetIncomeVariance(), dto.getNetIncomeVariancePct(), BOLD_DATA_FONT);

            doc.add(table);
            doc.close();
            return out.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Income Statement PDF", ex);
        }
    }


    public byte[] exportBalanceSheet(BalanceSheetReportDTO dto) {
        Document doc = new Document(PageSize.A4, 36, 36, 72, 72);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Title
            Paragraph title = new Paragraph("Balance Sheet", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            // As-of Date
            Paragraph date = new Paragraph("As-of Date: " + dto.getAsOfDate().format(DATE_FMT));
            date.setAlignment(Element.ALIGN_CENTER);
            doc.add(date);

            // Validation Status
            boolean isBalanced = "Balanced".equalsIgnoreCase(dto.getValidationStatus());
            Font statusFont = isBalanced ? STATUS_BALANCED_FONT : STATUS_UNBALANCED_FONT;
            Paragraph status = new Paragraph("Status: " + dto.getValidationStatus(), statusFont);
            status.setAlignment(Element.ALIGN_CENTER);
            doc.add(status);

            doc.add(Chunk.NEWLINE);

            // Table definition (3 columns: ID, Name, Balance)
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 5f, 2f});

            // Column headers
            addPdfHeaderCell(table, "Account ID");
            addPdfHeaderCell(table, "Account Name");
            addPdfHeaderCell(table, "Balance");

            // --- Assets ---
            addPdfSectionHeader(table, "Current Assets");
            for (BalanceSheetAccountRowDTO row : dto.getAssetsCurrent()) {
                addPdfAccountRow(table, row);
            }

            addPdfSectionHeader(table, "Non-Current Assets");
            for (BalanceSheetAccountRowDTO row : dto.getAssetsNonCurrent()) {
                addPdfAccountRow(table, row);
            }
            addPdfTotalRow(table, "Total Assets", dto.getTotalAssets());

            // --- Liabilities ---
            addPdfSpacerRow(table);
            addPdfSectionHeader(table, "Current Liabilities");
            for (BalanceSheetAccountRowDTO row : dto.getLiabilitiesCurrent()) {
                addPdfAccountRow(table, row);
            }

            addPdfSectionHeader(table, "Non-Current Liabilities");
            for (BalanceSheetAccountRowDTO row : dto.getLiabilitiesNonCurrent()) {
                addPdfAccountRow(table, row);
            }
            addPdfTotalRow(table, "Total Liabilities", dto.getTotalLiabilities());

            // --- Equity ---
            addPdfSpacerRow(table);
            addPdfSectionHeader(table, "Equity");
            for (BalanceSheetAccountRowDTO row : dto.getEquity()) {
                addPdfAccountRow(table, row);
            }
            addPdfTotalRow(table, "Total Equity", dto.getTotalEquity());

            // --- Final Validation Total ---
            addPdfSpacerRow(table);
            BigDecimal totalLiabilitiesAndEquity = dto.getTotalLiabilities().add(dto.getTotalEquity());
            addPdfTotalRow(table, "Total Liabilities + Equity", totalLiabilitiesAndEquity);


            doc.add(table);
            doc.close();
            return out.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Balance Sheet PDF", ex);
        }
    }

    public byte[] exportCashFlow(CashFlowReportDTO report) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11);

            // Title
            Paragraph title = new Paragraph("Cash Flow Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Period: " + report.getPeriodLabel(), normalFont));
            document.add(Chunk.NEWLINE);

            // Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{3, 2});

            // Helper to add rows
            BiConsumer<String, String> addRow = (label, value) -> {
                PdfPCell c1 = new PdfPCell(new Phrase(label, headerFont));
                PdfPCell c2 = new PdfPCell(new Phrase(value, normalFont));
                c1.setBorder(Rectangle.BOX);
                c2.setBorder(Rectangle.BOX);
                table.addCell(c1);
                table.addCell(c2);
            };

            addRow.accept("Opening Cash Balance", report.getOpeningCash().toPlainString());
            addRow.accept("Cash Flow from Operations (CFO)", report.getCfo().toPlainString());
            addRow.accept("Cash Flow from Investing (CFI)", report.getCfi().toPlainString());
            addRow.accept("Cash Flow from Financing (CFF)", report.getCff().toPlainString());
            addRow.accept("Net Change in Cash", report.getNetChange().toPlainString());
            addRow.accept("Ending Cash Balance", report.getEndingCash().toPlainString());
            addRow.accept("Cash (from Balance Sheet)", report.getBalanceSheetCash().toPlainString());
            addRow.accept("Reconciled", report.isReconciled() ? "Yes" : "No");

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create pdf file");
        }

    }

    /**
     * PDF Helper to add a styled header cell.
     */
    private void addPdfHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    /**
     * PDF Helper to add a formatted data row.
     */
    private void addPdfRow(PdfPTable table, String label, BigDecimal actual, BigDecimal budget,
                           BigDecimal variance, BigDecimal variancePct, Font font) {
        // Label
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(4f);
        labelCell.setPaddingLeft(font == BOLD_DATA_FONT ? 10f : 5f); // Indent bold rows
        table.addCell(labelCell);

        // Actual
        table.addCell(createNumericPdfCell(actual, font));
        // Budget
        table.addCell(createNumericPdfCell(budget, font));
        // Variance
        table.addCell(createNumericPdfCell(variance, font));
        // Variance %
        table.addCell(createPercentagePdfCell(variancePct, font));
    }

    /**
     * PDF Helper to create a single right-aligned, formatted numeric cell.
     */
    private PdfPCell createNumericPdfCell(BigDecimal value, Font font) {
        String formatted = (value != null) ? DEC.format(value) : "-";
        PdfPCell cell = new PdfPCell(new Phrase(formatted, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4f);
        return cell;
    }

    /**
     * PDF Helper for percentage cells.
     */
    private PdfPCell createPercentagePdfCell(BigDecimal value, Font font) {
        String formatted = (value != null) ? DEC.format(value) + "%" : "-";
        PdfPCell cell = new PdfPCell(new Phrase(formatted, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4f);
        return cell;
    }

    /**
     * PDF Helper to add a blue, spanning section header.
     */
    private void addPdfSectionHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SECTION_HEADER_FONT));
        cell.setColspan(3);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(4f);
        table.addCell(cell);
    }

    /**
     * PDF Helper to add a single account row.
     */
    private void addPdfAccountRow(PdfPTable table, BalanceSheetAccountRowDTO row) {
        // ID
        PdfPCell idCell = new PdfPCell(new Phrase(row.getAccountId(), DATA_FONT));
        idCell.setPadding(4f);
        idCell.setPaddingLeft(10f); // Indent
        table.addCell(idCell);

        // Name
        PdfPCell nameCell = new PdfPCell(new Phrase(row.getAccountName(), DATA_FONT));
        nameCell.setPadding(4f);
        table.addCell(nameCell);

        // Balance
        PdfPCell balCell = createNumericPdfCell(row.getSignedBalance(), DATA_FONT);
        table.addCell(balCell);
    }

    /**
     * PDF Helper to add a bold total row.
     */
    private void addPdfTotalRow(PdfPTable table, String label, BigDecimal total) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, TOTAL_FONT));
        labelCell.setColspan(2);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(4f);
        table.addCell(labelCell);

        PdfPCell totalCell = createNumericPdfCell(total, TOTAL_FONT);
        table.addCell(totalCell);
    }


    /**
     * PDF Helper to add a blank spacer row.
     */
    private void addPdfSpacerRow(PdfPTable table) {
        PdfPCell spacer = new PdfPCell(new Phrase(" "));
        spacer.setColspan(3);
        spacer.setFixedHeight(8f);
        spacer.setBorder(Rectangle.NO_BORDER);
        table.addCell(spacer);
    }
}
