package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrialBalanceRowDTO {
    private String accountId;
    private String accountName;
    private AccountType accountType;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;
    private boolean abnormal;

    public TrialBalanceRowDTO(String accountId, String accountName, AccountType accountType, BigDecimal totalDebits, BigDecimal totalCredits) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.totalDebits = totalDebits == null ? BigDecimal.ZERO : totalDebits;
        this.totalCredits = totalCredits == null ? BigDecimal.ZERO : totalCredits;
    }
}
