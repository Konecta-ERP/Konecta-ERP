package com.konecta.financeservice.dto;

import com.konecta.financeservice.entity.Period;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalTransactionDTO {

    private Long transactionId;

    private Long periodId;

    private LocalDate transactionDate;

    private String description;

    private List<JournalEntryDTO> entries;

    private String postedByUserId;

    private LocalDateTime createdAt;
}
