package com.konecta.identity_service.exception;

public class InvalidRequestException extends BaseApiException {
    public InvalidRequestException(String systemMessage, String clientMessage) {
        super(systemMessage, clientMessage);
    }
}