package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForecastRequestDTO {
    public Double revenueTwoQuartersAgo; // t-2
    public Double revenueLastQuarter;    // t-1
}
