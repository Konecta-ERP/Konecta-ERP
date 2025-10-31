package com.konecta.identity_service.exception;

import lombok.Getter;

@Getter
public abstract class BaseApiException extends RuntimeException {
    private final String clientMessage;
    public BaseApiException(String systemMessage, String clientMessage) {
        super(systemMessage); // The system message is stored in the default RuntimeException 'message' field
        this.clientMessage = clientMessage;
    }
}