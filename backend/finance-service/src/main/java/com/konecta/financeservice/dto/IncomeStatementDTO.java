package com.konecta.financeservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class IncomeStatementDTO {
    // period metadata
    private Long periodId;
    private String periodLabel;
    private LocalDate startDate;
    private LocalDate endDate;

    // Actuals
    private BigDecimal revenueActual;
    private BigDecimal cogsActual;
    private BigDecimal opexActual;
    private BigDecimal otherIncomeActual;
    private BigDecimal otherExpenseActual;

    // Budgets (from Period)
    private BigDecimal revenueBudget;
    private BigDecimal cogsBudget;
    private BigDecimal opexBudget;
    private BigDecimal otherIncomeBudget;
    private BigDecimal otherExpenseBudget;

    // Derived actuals
    private BigDecimal grossProfitActual;   // revenueActual - cogsActual
    private BigDecimal ebitActual;         // grossProfitActual - opexActual
    private BigDecimal netIncomeActual;    // ebitActual + otherIncomeActual - otherExpenseActual

    // Derived budgets (same formulas)
    private BigDecimal grossProfitBudget;
    private BigDecimal ebitBudget;
    private BigDecimal netIncomeBudget;

    // Variances (actual - budget)
    private BigDecimal revenueVariance;
    private BigDecimal cogsVariance;
    private BigDecimal grossProfitVariance;
    private BigDecimal ebitVariance;
    private BigDecimal netIncomeVariance;

    // Variance percentages (variance / budget * 100). null if budget == 0
    private BigDecimal revenueVariancePct;
    private BigDecimal grossProfitVariancePct;
    private BigDecimal ebitVariancePct;
    private BigDecimal netIncomeVariancePct;
}
