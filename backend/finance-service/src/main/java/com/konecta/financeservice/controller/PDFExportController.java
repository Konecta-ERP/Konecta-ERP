package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.*;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.AnalyticsService;
import com.konecta.financeservice.service.PDFExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pdf")
public class PDFExportController {

    private final AnalyticsService analyticsService;
    private final PDFExportService pdfExportService;

    @Autowired
    public PDFExportController(AnalyticsService analyticsService, PDFExportService pdfExportService) {
        this.analyticsService = analyticsService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("trial-balance/{id}")
    public ResponseEntity<byte[]> exportTrialBalance(@PathVariable("id") Long periodId) {
        TrialBalanceReportDTO report = analyticsService.generateTrialBalance(periodId);
        byte[] bytes = pdfExportService.exportTrialBalance(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trial_balance.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/gl")
    public ResponseEntity<byte[]> exportGL(@RequestParam("fromDate") LocalDate fromDate,
                                           @RequestParam("toDate") LocalDate toDate,
                                           @RequestParam(name = "accountPKs", required = false) List<Long> accountPKs) throws IOException {
        GLResponseDTO response = analyticsService.generateGLResponse(fromDate, toDate, accountPKs);
        byte[] bytes = pdfExportService.exportGl(response);

        String filename = "GL_" + fromDate + "_" + toDate + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/income-statement/{id}")
    public ResponseEntity<byte[]> exportIncomeStatement(@PathVariable("id") Long periodId) throws IOException {
        IncomeStatementDTO report = analyticsService.generateIncomeStatement(periodId);
        byte[] bytes = pdfExportService.exportIncomeStatement(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/balance-sheet/{date}")
    public ResponseEntity<byte[]> exportBalanceSheet(@PathVariable("date") LocalDate asOfDate) {
        BalanceSheetReportDTO dto = analyticsService.generateBalanceSheet(asOfDate);
        byte[] bytes = pdfExportService.exportBalanceSheet(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/cash-flow/{id}")
    public ResponseEntity<byte[]> exportCashFlowPdf(@PathVariable("id") Long periodId) throws IOException {
        CashFlowReportDTO report = analyticsService.generateCashFlow(periodId);
        byte[] bytes = pdfExportService.exportCashFlow(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cash_flow.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}


