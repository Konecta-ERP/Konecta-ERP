package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.*;
import com.konecta.financeservice.service.AnalyticsService;
import com.konecta.financeservice.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/excel")
public class ExcelExportController {

    private final AnalyticsService analyticsService;
    private final ExcelExportService excelExportService;

    @Autowired
    public ExcelExportController(AnalyticsService analyticsService, ExcelExportService excelExportService) {
        this.analyticsService = analyticsService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/trial-balance/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportTrialBalance(@PathVariable("id") Long periodId) throws IOException {
        TrialBalanceReportDTO report = analyticsService.generateTrialBalance(periodId);
        byte[] bytes = excelExportService.exportTrialBalance(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trial_balance.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/gl")
    @PreAuthorize("hasAuthority('CFO')")
    public ResponseEntity<byte[]> exportGL(@RequestParam("fromDate") LocalDate fromDate,
                                           @RequestParam("toDate") LocalDate toDate,
                                           @RequestParam(name = "accountPKs", required = false) List<Long> accountPKs) throws IOException {
        GLResponseDTO response = analyticsService.generateGLResponse(fromDate, toDate, accountPKs);
        byte[] bytes = excelExportService.exportGl(response);

        String filename = "GL_" + fromDate + "_" + toDate + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/income-statement/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportIncomeStatement(@PathVariable("id") Long periodId) throws IOException {
        IncomeStatementDTO report = analyticsService.generateIncomeStatement(periodId);
        byte[] bytes = excelExportService.exportIncomeStatement(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_statement.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/balance-sheet/{date}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportBalanceSheet(@PathVariable("date") LocalDate asOfDate) throws IOException {
        BalanceSheetReportDTO dto = analyticsService.generateBalanceSheet(asOfDate);
        byte[] bytes = excelExportService.exportBalanceSheet(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance_sheet.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/cash-flow/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<byte[]> exportCashFlowReport(@PathVariable("id") Long periodId) throws IOException {
        CashFlowReportDTO dto = analyticsService.generateCashFlow(periodId);
        byte[] bytes = excelExportService.exportCashFlow(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cash_flow.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

}
