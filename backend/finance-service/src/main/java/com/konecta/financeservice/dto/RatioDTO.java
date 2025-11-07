package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatioDTO {

    private Long ratioId;
    private String ratioName;
    private BigDecimal benchmarkValue = BigDecimal.ZERO;
    private BigDecimal warningThreshold = BigDecimal.ZERO;

}
