package com.konecta.employeeservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailsDto {

  // From Employee entity
  private Integer employeeId; // The internal employee ID
  private String positionTitle;
  private LocalDate hireDate;
  private BigDecimal salaryGross;
  private BigDecimal salaryNet;

  // From User entity
  private UUID userId; // The main user ID
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;

  // From Department entity
  private String departmentName;
}
