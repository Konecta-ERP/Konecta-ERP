from flask import Flask, render_template, request, jsonify
from flask_cors import CORS
import uuid
import re
from .retrieval import get_retriever
from .generator import (
    get_llm, 
    create_rag_chain, 
    get_session_history,
    get_session_state,
    set_session_state,
    get_workflow_context,
    set_workflow_context,
    clear_workflow_context,
    create_query_correction_chain  # <-- 1. IMPORT NEW FUNCTION
)
from .guardrails import check_response_confidence, filter_content
from .workflows import handle_workflow_step

import py_eureka_client.eureka_client as eureka_client

eureka_client.init(
    eureka_server="http://discovery-server:8761/eureka", # URL from docker-compose
    app_name="AI-CHATBOT",                               # Service Name (Uppercase)
    instance_port=5000,                                  # The port Flask runs on
    instance_host="chatbot-instance"                     # The docker container name
)

app = Flask(__name__, template_folder='../templates', static_folder='../static')
# CORS(app)  # Enable Cross-Origin Resource Sharing

# --- GLOBAL INITIALIZATION ---
try:
    retriever = get_retriever(top_k=3)
    llm = get_llm()
    rag_chain_with_memory = create_rag_chain(retriever, llm)
    query_correction_chain = create_query_correction_chain(llm) # <-- 2. INITIALIZE NEW CHAIN
    print("\n--- Flask App is Ready to Go! ---")
    print("Access at: http://127.0.0.1:5000")

except Exception as e:
    print(f"\n!!! FAILED TO INITIALIZE RAG PIPELINE !!!")
    print(f"Error: {e}")
    print("Please ensure your GOOGLE_API_KEY is set in 'generator.py' and all dependencies are installed.")
    rag_chain_with_memory = None
# ------------------------------


@app.route('/')
def index():
    """Serves the main HTML page."""
    return render_template('index.html')

@app.route('/chat', methods=['POST'])
def chat():
    """Handles the chat interaction."""
    
    if not rag_chain_with_memory:
         return jsonify({"error": "RAG pipeline is not initialized. Check API key."}), 500

    try:
        data = request.json
        user_message = data.get('message')
        session_id = data.get('session_id')

        if not user_message:
            return jsonify({"error": "No message provided."}), 400
        if not session_id:
            return jsonify({"error": "No session_id provided."}), 400

        # --- 1. Content Filtering (Guardrail) ---
        filtered_message = filter_content(user_message)
        if filtered_message != user_message:
            return jsonify({"answer": filtered_message})
        
        # --- 2. Get current state, history, and context ---
        current_state = get_session_state(session_id)
        current_context = get_workflow_context(session_id)
        history = get_session_history(session_id)

        # --- 3. Workflow Intent Detection & Handling ---
        (workflow_response, new_state, updated_context) = handle_workflow_step(
            user_message, current_state, current_context
        )
        
        set_session_state(session_id, new_state)
        set_workflow_context(session_id, updated_context)

        if workflow_response:
            history.add_user_message(user_message)
            history.add_ai_message(workflow_response)
            return jsonify({"answer": workflow_response, "sources": []})

        # --- 4. NEW: Query Correction Step ---
        # This runs ONLY if no workflow was triggered
        print(f"[{session_id} | {current_state}] User: {user_message}")
        corrected_message = query_correction_chain.invoke({"question": user_message})
        print(f"[{session_id} | {current_state}] Corrected Query: {corrected_message}")
        
        # --- 5. RAG Pipeline ---
        config = {"configurable": {"session_id": session_id}}
        # We now use the 'corrected_message' for the RAG chain
        response_text = rag_chain_with_memory.invoke({"question": corrected_message}, config=config)
        
        # --- 6. Confidence/Citation Check (Guardrail) ---
        final_response = check_response_confidence(response_text)
        
        print(f"[{session_id} | {new_state}] Bot: {final_response}")

        sources = re.findall(r"\[Source: (.*?), Page: (.*?)\]", final_response)
        
        return jsonify({
            "answer": final_response.split("[Source:")[0].strip(),
            "sources": [{"name": s[0], "page": s[1]} for s in sources]
        })

    except Exception as e:
        print(f"Error during chat: {e}")
        return jsonify({"error": str(e)}), 500

def run_app():
    """Runs the Flask application."""
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False)

if __name__ == '__main__':
    run_app()
