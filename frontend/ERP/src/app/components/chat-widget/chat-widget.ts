import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { ChatService, ChatSource } from '../../core/services/chat.service';
import { SharedModule } from '../../shared/module/shared/shared-module';

interface Message {
    sender: 'user' | 'bot';
    text: string;
    sources?: ChatSource[];
    timestamp: Date;
}

@Component({
    selector: 'app-chat-widget',
    standalone: true,
    imports: [SharedModule],
    templateUrl: './chat-widget.html',
    styleUrls: ['./chat-widget.css'], // Make sure this points to .css
})
export class ChatWidget implements AfterViewChecked {
    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

    visible: boolean = false;
    userInput: string = '';
    isLoading: boolean = false;

    // Start with an empty array or a welcome message
    messages: Message[] = [
        {
            sender: 'bot',
            text: 'Hello! I am your Konecta ERP Assistant. Please ask me anything.',
            timestamp: new Date(),
        },
    ];

    constructor(private chatService: ChatService) {}

    toggleChat() {
        this.visible = !this.visible;
    }

    sendMessage() {
        if (!this.userInput.trim() || this.isLoading) return;

        const text = this.userInput;
        this.userInput = '';

        // 1. Add User Message immediately
        this.messages.push({ sender: 'user', text, timestamp: new Date() });
        this.isLoading = true;
        this.scrollToBottom();

        this.chatService.sendMessage(text).subscribe({
            next: (response) => {
                this.isLoading = false;
                // 2. Start the typing effect
                this.typeMessage(response.answer, response.sources);
            },
            error: (err) => {
                console.error(err);
                this.isLoading = false;
                this.messages.push({
                    sender: 'bot',
                    text: 'Sorry, I am having trouble connecting to the server.',
                    timestamp: new Date(),
                });
            },
        });
    }

    // === NEW: Typing Effect Logic ===
    typeMessage(fullText: string, sources: ChatSource[] = []) {
        // 1. Create a placeholder message with empty text
        const newMessage: Message = {
            sender: 'bot',
            text: '',
            sources: undefined, // Don't show sources until typing is done
            timestamp: new Date(),
        };
        this.messages.push(newMessage);

        // 2. Loop through characters
        let index = 0;
        const typingSpeed = 20; // milliseconds per character (lower is faster)

        const interval = setInterval(() => {
            if (index < fullText.length) {
                // Append next character
                newMessage.text += fullText.charAt(index);
                index++;
                this.scrollToBottom(); // Keep scrolling as it types
            } else {
                // 3. Typing finished
                clearInterval(interval);
                // Show sources now that text is complete
                newMessage.sources = sources;
                this.scrollToBottom();
            }
        }, typingSpeed);
    }

    ngAfterViewChecked() {
        // Only auto-scroll if we aren't currently typing (the typing loop handles its own scrolling)
        // or if it's the very first load.
        // However, keeping it simple here is usually fine.
    }

    scrollToBottom(): void {
        try {
            this.scrollContainer.nativeElement.scrollTop =
                this.scrollContainer.nativeElement.scrollHeight;
        } catch (err) {}
    }
}
