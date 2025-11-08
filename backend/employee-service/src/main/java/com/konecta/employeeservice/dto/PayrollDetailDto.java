package com.konecta.employeeservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayrollDetailDto {
  private String type;
  private String description;
  private BigDecimal amount;
}
