package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ForecastResponseDTO {
    public double predictedNextQuarterRevenue;
}
