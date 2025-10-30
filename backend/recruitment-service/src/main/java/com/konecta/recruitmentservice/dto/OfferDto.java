package com.konecta.recruitmentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferDto {
  private Integer id;
  private BigDecimal netSalary;
  private BigDecimal grossSalary;
  private Map<String, Object> benefits;
  private LocalDate startDate;
  private Integer applicantId;
}