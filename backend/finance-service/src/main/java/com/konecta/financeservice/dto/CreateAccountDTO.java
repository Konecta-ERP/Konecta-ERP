package com.konecta.financeservice.dto;

import com.konecta.financeservice.model.enums.AccountType;
import com.konecta.financeservice.model.enums.CashSource;
import com.konecta.financeservice.model.enums.ProfitLossMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountDTO {

    @NotBlank
    private String accountId;

    private String accountName;

    @NotNull
    private AccountType accountType;

    private ProfitLossMapping plMapping;

    private CashSource cashSource;

    private Boolean isCashAccount;

    private Boolean isCurrent;

    private String description;

}

