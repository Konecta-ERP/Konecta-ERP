import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { v4 as uuidv4 } from 'uuid';
import { baseURL } from '../apiRoot/baseURL';

export interface ChatSource {
    name: string;
    page: string;
}

export interface ChatResponse {
    answer: string;
    sources: ChatSource[];
}

@Injectable({
    providedIn: 'root',
})
export class ChatService {
    private apiUrl = `${baseURL}/chatbot/chat`;
    private sessionId: string;

    constructor(private http: HttpClient) {
        // Persist session ID to keep conversation history across reloads
        let storedId = localStorage.getItem('chat_session_id');
        if (!storedId) {
            storedId = uuidv4();
            localStorage.setItem('chat_session_id', storedId);
        }
        this.sessionId = storedId;
    }

    sendMessage(message: string): Observable<ChatResponse> {
        return this.http.post<ChatResponse>(this.apiUrl, {
            message: message,
            session_id: this.sessionId,
        });
    }

    // Optional: Reset session if user wants to start over
    clearSession() {
        this.sessionId = uuidv4();
        localStorage.setItem('chat_session_id', this.sessionId);
    }
}
