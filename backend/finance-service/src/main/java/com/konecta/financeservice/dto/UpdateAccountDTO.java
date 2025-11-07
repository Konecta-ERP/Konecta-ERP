package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.AccountStatus;
import com.konecta.financeservice.model.enums.AccountType;
import com.konecta.financeservice.model.enums.CashSource;
import com.konecta.financeservice.model.enums.ProfitLossMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDTO {
    private String accountId;
    private String accountName;
    private AccountType accountType;
    private ProfitLossMapping plMapping;
    private CashSource cashSource;
    private Boolean isCashAccount;
    private Boolean isCurrent;
    private String description;
}
