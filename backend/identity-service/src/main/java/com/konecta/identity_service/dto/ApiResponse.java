package com.konecta.identity_service.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "data", "status", "cMessage", "sMessage" })
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApiResponse<T> {

    @JsonProperty("status")
    private int status;

    @JsonProperty("sMessage")
    private String sMessage;

    @JsonProperty("cMessage")
    private String cMessage;

    @JsonProperty("data")
    private T data;

    private ApiResponse(int status, String sMessage, String cMessage, T data) {
        this.status = status;
        this.sMessage = sMessage;
        this.cMessage = cMessage;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data, int status, String cMessage, String sMessage) {
        return new ApiResponse<>(status, sMessage, cMessage, data);
    }

    public static <T> ApiResponse<T> success(int status, String cMessage, String sMessage) {
        return new ApiResponse<>(status, sMessage, cMessage, null);
    }

    public static <T> ApiResponse<T> error(int status, String sMessage, String cMessage) {
        return new ApiResponse<>(status, sMessage, cMessage, null);
    }
}