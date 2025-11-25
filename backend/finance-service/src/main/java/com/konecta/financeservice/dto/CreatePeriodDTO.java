package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.PeriodStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePeriodDTO {
    @NotBlank
    private String label;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private PeriodStatus status;

    @NotNull
    private String createdByUserId;

    private BigDecimal revenueBudget;

    private BigDecimal cogsBudget;

    private BigDecimal opexBudget;

    private BigDecimal otherIncomeBudget;

    private BigDecimal otherExpenseBudget;

}
