package com.konecta.financeservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateJournalTransactionDTO {

    @NotNull
    private LocalDate transactionDate;

    private String description;

    @NotNull
    private Long postedByUserId;

    @NotNull
    private List<CreateJournalEntryDTO> entries;
}
