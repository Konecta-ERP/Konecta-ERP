package com.konecta.employeeservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequestDto {

  // User fields
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;

  // Employee fields
  private String positionTitle;
  private LocalDate hireDate;
  private BigDecimal salaryGross;
  private BigDecimal salaryNet;

  // Link to Department
  private Integer departmentId;
}
