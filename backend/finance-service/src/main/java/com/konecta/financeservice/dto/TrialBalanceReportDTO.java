package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrialBalanceReportDTO {
    private String periodLabel;
    private String periodStatus;
    private List<TrialBalanceRowDTO> rows;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private String tbStatus; // "Balanced" or "Unbalanced"

}
