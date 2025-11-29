package com.konecta.recruitmentservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.konecta.recruitmentservice.dto.CreateRequisitionDto;
import com.konecta.recruitmentservice.dto.JobRequisitionDto;
import com.konecta.recruitmentservice.dto.UpdateRequisitionDto;
import com.konecta.recruitmentservice.dto.UpdateRequisitionStatusDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import com.konecta.recruitmentservice.service.RequisitionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/job-requisitions")
public class RequisitionController {

  private final RequisitionService requisitionService;

  @Autowired
  public RequisitionController(RequisitionService requisitionService) {
    this.requisitionService = requisitionService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<JobRequisitionDto>> createRequisition(
      @Valid @RequestBody CreateRequisitionDto dto) {
    JobRequisitionDto newReq = requisitionService.createRequisition(dto);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        newReq,
        HttpStatus.CREATED.value(),
        "Job requisition created successfully.",
        "Requisition created with id " + newReq.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/search")
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<List<JobRequisitionDto>>> searchRequisitions(
      @RequestParam(name = "departmentId", required = false) Integer departmentId,
      @RequestParam(name = "status", required = false) RequisitionStatus status) {

    List<JobRequisitionDto> requisitions = requisitionService.searchRequisitions(departmentId, status);
    ApiResponse<List<JobRequisitionDto>> response = ApiResponse.success(
        requisitions,
        HttpStatus.OK.value(),
        "Requisitions retrieved successfully.",
        "Found " + requisitions.size() + " requisitions.");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<JobRequisitionDto>> getRequisition(
      @PathVariable Integer id) {
    JobRequisitionDto req = requisitionService.getRequisition(id);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        req,
        HttpStatus.OK.value(),
        "Requisition retrieved.",
        "Successfully retrieved requisition id " + id);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<JobRequisitionDto>> updateRequisition(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateRequisitionDto dto) {

    JobRequisitionDto updatedReq = requisitionService.updateRequisition(id, dto);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        updatedReq,
        HttpStatus.OK.value(),
        "Requisition updated successfully.",
        "Requisition " + id + " was updated.");
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAuthority('HR_MANAGER')")
  public ResponseEntity<ApiResponse<JobRequisitionDto>> updateRequisitionStatus(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateRequisitionStatusDto dto) {

    JobRequisitionDto updatedReq = requisitionService.updateRequisitionStatus(id, dto);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        updatedReq,
        HttpStatus.OK.value(),
        "Requisition status updated.",
        "Requisition " + id + " status set to " + dto.getStatus());
    return ResponseEntity.ok(response);
  }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_MANAGER')")
    public ResponseEntity<ApiResponse<?>> deleteRequisition(
            @PathVariable Integer id) {
        requisitionService.deleteRequisition(id);
        ApiResponse<JobRequisitionDto> response = ApiResponse.success(
                204,
                "Requisition retrieved.",
                "Successfully retrieved requisition id " + id);
        return ResponseEntity.ok(response);
    }
}