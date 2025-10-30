package com.konecta.recruitmentservice.controller;

import com.konecta.recruitmentservice.dto.CreateRequisitionDto;
import com.konecta.recruitmentservice.dto.JobRequisitionDto;
import com.konecta.recruitmentservice.dto.UpdateRequisitionDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import com.konecta.recruitmentservice.service.RequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-requisitions")
public class RequisitionController {

  private final RequisitionService requisitionService;

  @Autowired
  public RequisitionController(RequisitionService requisitionService) {
    this.requisitionService = requisitionService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<JobRequisitionDto>> createRequisition(
      @RequestBody CreateRequisitionDto dto) {
    JobRequisitionDto newReq = requisitionService.createRequisition(dto);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        newReq,
        HttpStatus.CREATED.value(),
        "Job requisition created successfully.",
        "Requisition created with id " + newReq.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<JobRequisitionDto>>> searchRequisitions(
      @RequestParam(required = false) Integer departmentId,
      @RequestParam(required = false) RequisitionStatus status) {

    List<JobRequisitionDto> requisitions = requisitionService.searchRequisitions(departmentId, status);
    ApiResponse<List<JobRequisitionDto>> response = ApiResponse.success(
        requisitions,
        HttpStatus.OK.value(),
        "Requisitions retrieved successfully.",
        "Found " + requisitions.size() + " requisitions.");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
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
  public ResponseEntity<ApiResponse<JobRequisitionDto>> updateRequisition(
      @PathVariable Integer id,
      @RequestBody UpdateRequisitionDto dto) {

    JobRequisitionDto updatedReq = requisitionService.updateRequisition(id, dto);
    ApiResponse<JobRequisitionDto> response = ApiResponse.success(
        updatedReq,
        HttpStatus.OK.value(),
        "Requisition updated successfully.",
        "Requisition " + id + " was updated.");
    return ResponseEntity.ok(response);
  }
}