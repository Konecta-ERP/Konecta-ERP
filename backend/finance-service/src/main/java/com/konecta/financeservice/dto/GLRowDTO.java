package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GLRowDTO {
    private Long entryId;
    private Long transactionId;
    private LocalDate transactionDate;
    private Long accountPK;
    private String accountName;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal signedAmount;   // debit - credit
    private BigDecimal runningBalance; // computed in Java
    private String description;
}
