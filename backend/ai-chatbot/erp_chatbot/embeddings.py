from langchain_huggingface import HuggingFaceEmbeddings  # <-- THIS LINE IS FIXED

def get_embedding_model():
    """
    Initializes and returns the HuggingFace sentence-transformer model
    for creating vector embeddings.
    """
    # Use the open-source model requested
    model_name = "sentence-transformers/all-MiniLM-L6-v2"
    model_kwargs = {'device': 'cpu'}  # Use CPU
    encode_kwargs = {'normalize_embeddings': False}
    
    embeddings = HuggingFaceEmbeddings(
        model_name=model_name,
        model_kwargs=model_kwargs,
        encode_kwargs=encode_kwargs
    )
    return embeddings
