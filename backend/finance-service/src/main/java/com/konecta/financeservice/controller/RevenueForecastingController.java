package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.ForecastRequestDTO;
import com.konecta.financeservice.dto.ForecastResponseDTO;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.RevenueForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("/api/finance/forecast"))
public class RevenueForecastingController {

    private final RevenueForecastingService forecastingService;

    @Autowired
    public RevenueForecastingController(RevenueForecastingService forecastingService) {
        this.forecastingService = forecastingService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CFO') or hasAuthority('ACCOUNTANT')")
    public ResponseEntity<?> getForecast(@RequestBody ForecastRequestDTO request) {
        try {
            if (request.revenueTwoQuartersAgo == null || request.revenueLastQuarter == null) {
                return ResponseEntity.badRequest().body("Error: Both revenue figures are required.");
            }
            double[] inputData = {request.revenueTwoQuartersAgo, request.revenueLastQuarter};
            ForecastResponseDTO predictedRevenue = forecastingService.predictNextQuarter(inputData);
            ApiResponse<ForecastResponseDTO> response = ApiResponse.success(
                    predictedRevenue,
                    HttpStatus.OK.value(),
                    "Prediction completed",
                    "Prediction completed"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Prediction failed: " + e.getMessage());
        }
    }
}
