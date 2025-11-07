package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
  private Integer id;
  private RequestType requestType;
  private String reason;
  private LocalDate startDate;
  private LocalDate endDate;
  private RequestStatus status;
  private Integer employeeId;
  private String employeeName;
}
