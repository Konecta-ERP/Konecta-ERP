package com.konecta.employeeservice.dto;

import lombok.Data;

@Data
public class CreateOrUpdateDepartmentDto {
  private String name;

  // The internal Employee ID of the manager
  private Integer managerId;
}
