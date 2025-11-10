package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.CreateJournalTransactionDTO;
import com.konecta.financeservice.dto.JournalTransactionDTO;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.JournalTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/journal-transactions")
public class JournalTransactionController {

    private final JournalTransactionService journalTransactionService;

    @Autowired
    public JournalTransactionController(JournalTransactionService journalTransactionService) {
        this.journalTransactionService = journalTransactionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<JournalTransactionDTO>> createJournalTransaction(@RequestBody CreateJournalTransactionDTO dto) {
        JournalTransactionDTO journalTransaction = journalTransactionService.createJournalTransaction(dto);
        ApiResponse<JournalTransactionDTO> response = ApiResponse.success(
                journalTransaction,
                HttpStatus.CREATED.value(),
                "Journal transaction created successfully",
                "Journal transaction created successfully with PK " + journalTransaction.getTransactionId()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
