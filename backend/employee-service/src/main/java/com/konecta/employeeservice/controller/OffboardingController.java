package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.InitiateOffboardingDto;
import com.konecta.employeeservice.dto.OffboardingChecklistDto;
import com.konecta.employeeservice.dto.UpdateChecklistDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.OffboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OffboardingController {

  private final OffboardingService offboardingService;

  @Autowired
  public OffboardingController(OffboardingService offboardingService) {
    this.offboardingService = offboardingService;
  }

  @PostMapping("/employees/{employeeId}/offboard")
  public ResponseEntity<ApiResponse<OffboardingChecklistDto>> initiateOffboarding(
      @PathVariable Integer employeeId,
      @RequestBody InitiateOffboardingDto dto) {

    OffboardingChecklistDto checklist = offboardingService.initiateOffboarding(employeeId, dto);
    ApiResponse<OffboardingChecklistDto> response = ApiResponse.success(
        checklist,
        HttpStatus.CREATED.value(),
        "Offboarding process initiated.",
        "OffboardingChecklist created with id " + checklist.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/employees/{employeeId}/offboarding-checklist")
  public ResponseEntity<ApiResponse<OffboardingChecklistDto>> getChecklist(
      @PathVariable Integer employeeId) {

    OffboardingChecklistDto checklist = offboardingService.getChecklistForEmployee(employeeId);
    ApiResponse<OffboardingChecklistDto> response = ApiResponse.success(
        checklist,
        HttpStatus.OK.value(),
        "Checklist retrieved.",
        "Successfully retrieved OffboardingChecklist id " + checklist.getId());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/offboarding-checklists/{checklistId}")
  public ResponseEntity<ApiResponse<OffboardingChecklistDto>> updateChecklist(
      @PathVariable Integer checklistId,
      @RequestBody UpdateChecklistDto dto) {

    OffboardingChecklistDto checklist = offboardingService.updateChecklist(checklistId, dto);
    ApiResponse<OffboardingChecklistDto> response = ApiResponse.success(
        checklist,
        HttpStatus.OK.value(),
        "Checklist updated.",
        "Successfully updated OffboardingChecklist id " + checklistId);
    return ResponseEntity.ok(response);
  }
}