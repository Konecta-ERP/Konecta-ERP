document.addEventListener("DOMContentLoaded", () => {
    const chatBox = document.getElementById("chat-box");
    const userInput = document.getElementById("user-input");
    const sendBtn = document.getElementById("send-btn");
    const loading = document.getElementById("loading-indicator");
    
    let sessionId = uuidv4(); // Generate a unique session ID for this chat

    // Function to generate a simple UUID
    function uuidv4() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    // Function to add a message to the chat box
    function addMessage(text, sender, sources = []) {
        const messageDiv = document.createElement("div");
        messageDiv.classList.add("message", sender);
        
        let messageHTML = text.replace(/\n/g, '<br>'); // Convert newlines to breaks

        if (sources.length > 0) {
            messageHTML += '<div class="sources"><strong>Sources:</strong><br>';
            sources.forEach(src => {
                messageHTML += `<span class="source">${src.name} (Page: ${src.page})</span>`;
            });
            messageHTML += '</div>';
        }

        messageDiv.innerHTML = messageHTML;
        chatBox.appendChild(messageDiv);
        chatBox.scrollTop = chatBox.scrollHeight; // Auto-scroll to bottom
    }

    // Function to handle sending a message
    async function sendMessage() {
        const messageText = userInput.value.trim();
        if (messageText === "") return;

        addMessage(messageText, "user");
        userInput.value = "";
        loading.style.display = "block"; // Show loading spinner

        try {
            const response = await fetch("/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    "message": messageText,
                    "session_id": sessionId
                }),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            
            if (data.error) {
                addMessage(`Error: ${data.error}`, "bot");
            } else {
                addMessage(data.answer, "bot", data.sources);
            }

        } catch (error) {
            console.error("Error sending message:", error);
            addMessage("Sorry, something went wrong. Please try again.", "bot");
        } finally {
            loading.style.display = "none"; // Hide loading spinner
        }
    }

    // Event Listeners
    sendBtn.addEventListener("click", sendMessage);
    userInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            sendMessage();
        }
    });
});