package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.AccountStatus;
import com.konecta.financeservice.model.enums.AccountType;
import com.konecta.financeservice.model.enums.CashSource;
import com.konecta.financeservice.model.enums.ProfitLossMapping;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long accountPK;
    private String accountId;
    private String accountName;
    private AccountType accountType;
    private ProfitLossMapping plMapping;
    private CashSource cashSource;
    private boolean isCashAccount;
    private boolean isCurrent;
    private String description;
    private AccountStatus status;
    private boolean hasTransactions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
