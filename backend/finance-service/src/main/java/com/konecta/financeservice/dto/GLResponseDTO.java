package com.konecta.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GLResponseDTO {

    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> filteredAccounts;
    private List<GLRowDTO> entries;

}

