package com.konecta.employeeservice.dto;

import lombok.Data;

@Data
public class CreateFeedbackDto {
  private String feedback;
  private Integer giverId; // The ID of the employee *giving* the feedback
}
