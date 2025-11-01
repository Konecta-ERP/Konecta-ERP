package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.enums.RequestStatus;
import lombok.Data;

@Data
public class UpdateLeaveStatusDto {
  private RequestStatus status;
}
