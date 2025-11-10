package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.*;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.RatioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/ratios")
public class RatioController {

    private final RatioService ratioService;

    @Autowired
    public RatioController(RatioService ratioService) {
        this.ratioService = ratioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RatioDTO>> createRatio(@RequestBody CreateOrUpdateRatioDTO dto) {
        RatioDTO ratio = ratioService.createRatio(dto);
        ApiResponse<RatioDTO> response = ApiResponse.success(
                ratio,
                HttpStatus.CREATED.value(),
                "Ratio created successfully",
                "Ratio created successfully with PK " + ratio.getRatioId()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RatioDTO>> updateRatio(@PathVariable("id") Long id, @RequestBody CreateOrUpdateRatioDTO dto) {
        RatioDTO updatedRatio = ratioService.updateRatio(id, dto);
        ApiResponse<RatioDTO> response = ApiResponse.success(
                updatedRatio,
                HttpStatus.OK.value(),
                "Ratio updated successfully",
                "Ratio with id " + id + " updated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RatioDTO>>> getAllRatios() {
        List<RatioDTO> ratios = ratioService.getALlRatios();
        ApiResponse<List<RatioDTO>> response = ApiResponse.success(
                ratios,
                HttpStatus.OK.value(),
                "All ratios retrieved",
                ratios.size() + " ratios retrieved"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RatioDTO>> getRatio(@PathVariable("id") Long id) {
        RatioDTO ratio = ratioService.getRatio(id);
        ApiResponse<RatioDTO> response = ApiResponse.success(
                ratio,
                HttpStatus.OK.value(),
                "Ratio retrieved successfully",
                "Ratio with id " + id + " retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

}
