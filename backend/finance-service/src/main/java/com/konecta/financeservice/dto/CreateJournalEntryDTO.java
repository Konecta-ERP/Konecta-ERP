package com.konecta.financeservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateJournalEntryDTO {

    @NotNull
    private Long accountPK;

    private BigDecimal debitAmount = BigDecimal.ZERO;

    private BigDecimal creditAmount = BigDecimal.ZERO;
}
