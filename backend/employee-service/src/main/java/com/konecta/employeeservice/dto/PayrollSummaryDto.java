package com.konecta.employeeservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PayrollSummaryDto {
  private Integer employeeId;
  private String yearMonth;
  private BigDecimal basicPay;
  private BigDecimal overtimePay;
  private BigDecimal deductions;
  private BigDecimal netPay;
  private List<PayrollDetailDto> details;
}
