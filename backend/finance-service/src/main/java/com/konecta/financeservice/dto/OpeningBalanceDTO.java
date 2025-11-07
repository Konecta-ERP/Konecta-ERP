package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpeningBalanceDTO {
    private Long accountPK;
    private BigDecimal openingBalance; // signed (debit positive)
}
