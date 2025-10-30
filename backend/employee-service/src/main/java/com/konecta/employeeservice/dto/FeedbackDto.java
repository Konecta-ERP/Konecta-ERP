package com.konecta.employeeservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackDto {
  private Integer id;
  private String feedback;
  private LocalDateTime createdAt;
  private Integer recipientId;
  private Integer giverId;
}
