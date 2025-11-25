package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.*;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/trial-balance/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<TrialBalanceReportDTO>> getTrialBalanceReport(@PathVariable("id") Long periodId) {
        TrialBalanceReportDTO dto = analyticsService.generateTrialBalance(periodId);
        ApiResponse<TrialBalanceReportDTO> response = ApiResponse.success(
                dto,
                HttpStatus.OK.value(),
                "Trial balance report generated",
                "Trial Balance report for period with id " + periodId + " generated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gl")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<GLResponseDTO>> getGL(
            @RequestParam("fromDate") LocalDate fromDate,
            @RequestParam("toDate") LocalDate toDate,
            @RequestParam(name = "accountPKs", required = false) List<Long> accountPKs
    ) {
        GLResponseDTO dto = analyticsService.generateGLResponse(fromDate, toDate, accountPKs);
        ApiResponse<GLResponseDTO> response = ApiResponse.success(
                dto,
                HttpStatus.OK.value(),
                "General Ledger response generated",
                "General Ledger response from " + fromDate.toString() + " to " + toDate.toString() + " generated"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/income-statement/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<IncomeStatementDTO>> getIncomeStatement(@PathVariable("id") Long periodId) {
        IncomeStatementDTO dto = analyticsService.generateIncomeStatement(periodId);
        ApiResponse<IncomeStatementDTO> response = ApiResponse.success(
                dto,
                HttpStatus.OK.value(),
                "Income statement generated",
                "Income statement for period with id " + periodId + " generated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance-sheet/{date}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<BalanceSheetReportDTO>> getBalanceSheet(@PathVariable("date") LocalDate asOfDate) {
        BalanceSheetReportDTO dto = analyticsService.generateBalanceSheet(asOfDate);
        ApiResponse<BalanceSheetReportDTO> response = ApiResponse.success(
                dto,
                HttpStatus.OK.value(),
                "Balance sheet generated",
                "Balance sheet as of " + asOfDate.toString() + " generated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cash-flow/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<CashFlowReportDTO>> getCashFlowReport(@PathVariable("id") Long periodId) {
        CashFlowReportDTO dto = analyticsService.generateCashFlow(periodId);
        ApiResponse<CashFlowReportDTO> response = ApiResponse.success(
                dto,
                HttpStatus.OK.value(),
                "Cash flow statement generated",
                "Cash flow statement for period with id " + periodId + " generated successfully"
        );
        return ResponseEntity.ok(response);
    }

}
