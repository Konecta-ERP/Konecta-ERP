package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceSheetAccountRowDTO {
    private Long accountPk;
    private String accountId;
    private String accountName;
    private String accountType;   // "ASSET" | "LIABILITY" | "EQUITY"
    private Boolean isCurrent;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal signedBalance; // computed = debit-credit or credit-debit depending on type

}
