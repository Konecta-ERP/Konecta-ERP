package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.PeriodStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodDTO {
    private Long periodId;

    private String label;

    private LocalDate startDate;

    private LocalDate endDate;

    private PeriodStatus status;

    private String createdByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private Long timeToClose;

    private BigDecimal revenueBudget;

    private BigDecimal cogsBudget;

    private BigDecimal opexBudget;

    private BigDecimal otherIncomeBudget;

    private BigDecimal otherExpenseBudget;
}
