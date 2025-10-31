package com.konecta.identity_service.exception;

public class ResourceNotFoundException extends BaseApiException {
    public ResourceNotFoundException(String systemMessage, String clientMessage) {
        super(systemMessage, clientMessage);
    }
}