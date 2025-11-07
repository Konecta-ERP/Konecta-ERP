package com.konecta.recruitmentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MakeOfferDto {

  @NotNull(message = "Net salary is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Net salary must be greater than 0")
  private BigDecimal netSalary;

  @NotNull(message = "Gross salary is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Gross salary must be greater than 0")
  private BigDecimal grossSalary;

  private Map<String, Object> benefits;

  @NotNull(message = "Start date is required")
  @Future(message = "Start date must be in the future")
  private LocalDate startDate;
}