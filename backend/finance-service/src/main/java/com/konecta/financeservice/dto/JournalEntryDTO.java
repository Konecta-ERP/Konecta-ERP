package com.konecta.financeservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDTO {

    private Long entryID;

    private Long accountPK;

    private String accountId;

    private String accountName;

    private BigDecimal debitAmount = BigDecimal.ZERO;

    private BigDecimal creditAmount = BigDecimal.ZERO;
}
