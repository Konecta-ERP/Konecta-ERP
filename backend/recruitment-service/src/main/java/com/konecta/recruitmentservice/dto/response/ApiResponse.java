package com.konecta.recruitmentservice.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "data", "status", "cMessage", "sMessage" })
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApiResponse<T> {

  @JsonProperty("status")
  private int status; // Custom status code

  @JsonProperty("sMessage")
  private String sMessage; // System message (detailed)

  @JsonProperty("cMessage")
  private String cMessage; // Client message (user-friendly)

  @JsonProperty("data")
  private T data; // The actual data payload

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
