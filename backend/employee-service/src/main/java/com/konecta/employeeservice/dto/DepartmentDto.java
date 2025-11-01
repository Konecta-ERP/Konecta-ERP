package com.konecta.employeeservice.dto;

import lombok.Data;

@Data
public class DepartmentDto {
  private Integer id;
  private String name;
  private Integer managerId;
  private String managerName; // Helpful to show the manager's name
}
