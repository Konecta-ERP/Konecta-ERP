package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashFlowReportDTO {
    private Long periodId;
    private String periodLabel;
    private BigDecimal openingCash;
    private BigDecimal cfo; // net cash from operations (inflows - outflows)
    private BigDecimal cfi; // investing
    private BigDecimal cff; // financing
    private BigDecimal netChange;
    private BigDecimal endingCash;
    private BigDecimal balanceSheetCash;
    private boolean reconciled;
    private Map<String, BigDecimal> sectionDetails; // optional: per-mapping breakdown
}
