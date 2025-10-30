package com.konecta.recruitmentservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class OfferDto {
  private Integer id;
  private BigDecimal netSalary;
  private BigDecimal grossSalary;
  private Map<String, Object> benefits;
  private LocalDate startDate;
  private Integer applicantId;
}