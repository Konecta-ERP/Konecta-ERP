package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceSheetReportDTO {
    private LocalDate asOfDate;
    private List<BalanceSheetAccountRowDTO> assetsCurrent;
    private List<BalanceSheetAccountRowDTO> assetsNonCurrent;
    private List<BalanceSheetAccountRowDTO> liabilitiesCurrent;
    private List<BalanceSheetAccountRowDTO> liabilitiesNonCurrent;
    private List<BalanceSheetAccountRowDTO> equity; // equity usually non-current but grouped here

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private String validationStatus; // "Balanced" or "Unbalanced"

}
