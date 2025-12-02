from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
import re
import os

# This now stores all data for a session, including history, state, and context
session_data = {}

def get_llm():
    """
    Initializes and returns the ChatGoogleGenerativeAI model.
    """
    api_key = os.environ.get("GOOGLE_API_KEY") 

    if not api_key:
        raise ValueError("GOOGLE_API_KEY not found in environment variables!")

    if api_key == "YOUR_GOOGLE_API_KEY_HERE":
        print("="*50)
        print("WARNING: GOOGLE_API_KEY not set in /erp_chatbot/generator.py")
        print("The chatbot will not work until you add your key.")
        print("="*50)

    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=api_key,
        temperature=0.0,
        convert_system_message_to_human=True # Recommended for Gemini
    )
    return llm
# --- ADD THIS NEW FUNCTION ---

def create_query_correction_chain(llm):
    """
    Creates a separate chain to correct spelling and rephrase the user's question.
    """
    correction_prompt_template = ChatPromptTemplate.from_messages([
        ("system", (
            "You are an expert at rephrasing user questions to be optimal for a vector database search.\n"
            "Correct any misspellings and rewrite the question as a clear, standalone search query.\n"
            "Only output the corrected query, and nothing else."
        )),
        ("user", "Original query: {question}")
    ])
    
    return (
        correction_prompt_template
        | llm
        | StrOutputParser()
    )

def get_or_create_session(session_id: str) -> dict:
    """
    Retrieves or creates a new session entry in our in-memory store.
    """
    if session_id not in session_data:
        session_data[session_id] = {
            "history": ChatMessageHistory(),
            "state": "normal",  # Initial state
            "workflow_context": {} # To store temp data (e.g., employee_id)
        }
    return session_data[session_id]

def get_session_history(session_id: str) -> ChatMessageHistory:
    """
    Retrieves the chat history for a given session_id.
    """
    session = get_or_create_session(session_id)
    return session["history"]

def get_session_state(session_id: str) -> str:
    """
    Retrieves the current workflow state for a given session_id.
    """
    session = get_or_create_session(session_id)
    return session["state"]

def set_session_state(session_id: str, state: str):
    """
    Updates the workflow state for a given session_id.
    """
    session = get_or_create_session(session_id)
    session["state"] = state
    print(f"[{session_id}] State changed to: {state}")

def get_workflow_context(session_id: str) -> dict:
    """
    Gets the temporary data for an in-progress workflow.
    """
    session = get_or_create_session(session_id)
    return session["workflow_context"]

def set_workflow_context(session_id: str, context: dict):
    """
    Updates the temporary data for an in-progress workflow.
    """
    session = get_or_create_session(session_id)
    session["workflow_context"] = context
    
def clear_workflow_context(session_id: str):
    """
    Clears the temporary workflow data after completion or cancellation.
    """
    session = get_or_create_session(session_id)
    session["workflow_context"] = {}


def create_rag_chain(retriever, llm):
    """
    Creates the final RAG chain (unchanged from before).
    """
    
    prompt_template = ChatPromptTemplate.from_messages([
        ("system", (
            "You are an expert assistant for answering questions about the provided company documents (specifically, a Labor Law document).\n"
            "Use the provided context and the chat history to answer the user's question.\n"
            "Your answer must be based *only* on the retrieved context and history. Do not use any outside knowledge.\n"
            "If the answer is not found in the context, you MUST respond with: \"I'm not confident I can answer that based on the provided documents.\"\n\n"
            "**Response Guidelines:**\n"
            "1.  Answer the question directly and concisely.\n"
            "2.  For every piece of information you provide, you MUST cite its source.\n"
            "3.  Format citations as [Source: {{source_name}}, Page: {{page_number}}].\n"
            "4.  If the context includes multiple sources, cite all of them."
        )),
        MessagesPlaceholder(variable_name="chat_history"),
        ("user", "Question: {question}\n\nContext:\n{context}")
    ])
    
    def format_docs_with_metadata(docs):
        formatted_docs = []
        for doc in docs:
            source_name = doc.metadata.get('source', 'Unknown').split('/')[-1]
            page_number = doc.metadata.get('page', 'N/A')
            context_str = f"Source: {source_name}, Page: {page_number}\n"
            context_str += f"Content: {doc.page_content}"
            formatted_docs.append(context_str)
        return "\n\n---\n\n".join(formatted_docs)

    context_retriever_runnable = RunnablePassthrough.assign(
        context=(lambda x: x["question"]) | retriever | format_docs_with_metadata
    )

    rag_chain_base = (
        context_retriever_runnable
        | prompt_template
        | llm
        | StrOutputParser()
    )
    
    rag_chain_with_memory = RunnableWithMessageHistory(
        rag_chain_base,
        get_session_history,
        input_messages_key="question",
        history_messages_key="chat_history",
    )
    
    return rag_chain_with_memory
