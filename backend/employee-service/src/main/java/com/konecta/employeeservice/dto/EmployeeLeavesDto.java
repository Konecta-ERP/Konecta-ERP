package com.konecta.employeeservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmployeeLeavesDto {
  private EmployeeDetailsDto employee;
  private List<LeaveRequestDto> leaveRequests;
}
