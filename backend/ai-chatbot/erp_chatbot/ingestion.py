from langchain_community.document_loaders import PyMuPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
import tiktoken

# The dataset you provided
PDF_FILE_PATH = "Translation-of-Labor-law-No.-14-of-2025.pdf"

def load_documents(file_path=PDF_FILE_PATH):
    """
    Loads documents from the specified PDF file path.
    """
    print(f"Loading documents from {file_path}...")
    loader = PyMuPDFLoader(file_path)
    documents = loader.load()
    print(f"Loaded {len(documents)} pages.")
    return documents

def chunk_documents(documents):
    """
    Splits the loaded documents into chunks based on token count.
    Uses the requested 500 token chunk size and 50 token overlap.
    """
    print("Chunking documents...")
    # Use tiktoken to count tokens accurately for splitting
    tokenizer = tiktoken.get_encoding("cl100k_base")
    
    text_splitter = RecursiveCharacterTextSplitter.from_tiktoken_encoder(
        encoding_name="cl100k_base",
        chunk_size=500,        # 500 tokens
        chunk_overlap=50         # 50 tokens overlap
    )
    
    chunks = text_splitter.split_documents(documents)
    print(f"Created {len(chunks)} text chunks.")
    return chunks
