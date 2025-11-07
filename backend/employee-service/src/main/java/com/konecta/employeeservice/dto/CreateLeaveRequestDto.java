package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.enums.RequestType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateLeaveRequestDto {
  private RequestType requestType;
  private String reason;
  private LocalDate startDate;
  private LocalDate endDate;
}
