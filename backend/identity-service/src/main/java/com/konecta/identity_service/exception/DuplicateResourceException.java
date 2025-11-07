package com.konecta.identity_service.exception;

public class DuplicateResourceException extends BaseApiException {
    public DuplicateResourceException(String systemMessage, String clientMessage) {
        super(systemMessage, clientMessage);
    }
}