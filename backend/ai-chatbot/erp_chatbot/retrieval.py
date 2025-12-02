from langchain_community.vectorstores import FAISS
from .ingestion import load_documents, chunk_documents
from .embeddings import get_embedding_model
import time

# This will be our in-memory "database"
vector_store = None
retriever = None

def create_vector_store(docs, embeddings):
    """
    Creates an in-memory FAISS vector store from the documents and embedding model.
    """
    print("Creating vector store...")
    db = FAISS.from_documents(docs, embeddings)
    print("Vector store created successfully.")
    return db

def get_retriever(top_k=3):
    """
    Initializes all components and returns a retriever.
    This function is designed to be called once on app startup.
    """
    global vector_store, retriever
    
    if not retriever:
        start_time = time.time()
        print("Initializing RAG pipeline...")
        
        # 1. Embeddings
        embedding_model = get_embedding_model()
        
        # 2. Ingestion
        documents = load_documents()
        chunks = chunk_documents(documents)
        
        # 3. Vector Store
        vector_store = create_vector_store(chunks, embedding_model)
        
        # 4. Retriever
        retriever = vector_store.as_retriever(search_kwargs={"k": top_k})
        
        end_time = time.time()
        print(f"Pipeline ready. Startup time: {end_time - start_time:.2f} seconds.")
    
    return retriever
