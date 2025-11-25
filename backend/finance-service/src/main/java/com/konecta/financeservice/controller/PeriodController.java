package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.CreatePeriodDTO;
import com.konecta.financeservice.dto.PeriodDTO;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/periods")
public class PeriodController {

    private final PeriodService periodService;

    @Autowired
    public PeriodController(PeriodService periodService) {
        this.periodService = periodService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CFO')")
    public ResponseEntity<ApiResponse<PeriodDTO>> createPeriod(@RequestBody CreatePeriodDTO dto) {
        PeriodDTO period = periodService.createPeriod(dto);
        ApiResponse<PeriodDTO> response = ApiResponse.success(
                period,
                HttpStatus.CREATED.value(),
                "Period created successfully",
                "Period with id " + period.getPeriodId() + " created successfully"
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<PeriodDTO>>> getAllPeriods() {
        List<PeriodDTO> periods = periodService.getAllPeriods();
        ApiResponse<List<PeriodDTO>> response = ApiResponse.success(
                periods,
                HttpStatus.OK.value(),
                "All periods retrieved",
                periods.size() + " periods retrieved"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/last")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<PeriodDTO>>> getLastSixPeriods() {
        List<PeriodDTO> periods = periodService.getLastSixPeriods();
        ApiResponse<List<PeriodDTO>> response = ApiResponse.success(
                periods,
                HttpStatus.OK.value(),
                "Last six periods retrieved",
                periods.size() + " periods retrieved"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/start-closing/{id}")
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PeriodDTO>> startClosingPeriod(@PathVariable("id") Long id) {
        PeriodDTO period = periodService.startClosingPeriod(id);
        ApiResponse<PeriodDTO> response = ApiResponse.success(
                period,
                HttpStatus.OK.value(),
                "Period closing initiated successfully",
                "Period closing for period with id " + period.getPeriodId() + " initiated successfully, all checks passed"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/lock/{id}")
    @PreAuthorize("hasAuthority('CFO')")
    public ResponseEntity<ApiResponse<PeriodDTO>> lockPeriod(@PathVariable("id") Long id) {
        PeriodDTO period = periodService.lockPeriod(id);
        ApiResponse<PeriodDTO> response = ApiResponse.success(
                period,
                HttpStatus.OK.value(),
                "Period closed successfully",
                "Period with id " + period.getPeriodId() + " closed successfully"
        );
        return ResponseEntity.ok(response);
    }
}
