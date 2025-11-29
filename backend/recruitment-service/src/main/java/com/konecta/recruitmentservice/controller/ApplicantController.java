package com.konecta.recruitmentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.konecta.recruitmentservice.dto.ApplicantDto;
import com.konecta.recruitmentservice.dto.UpdateApplicantStatusDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.service.ApplicantService;

import jakarta.validation.Valid;

@RestController
public class ApplicantController {

  private final ApplicantService applicantService;

  @Autowired
  public ApplicantController(ApplicantService applicantService) {
    this.applicantService = applicantService;
  }

  @GetMapping("/api/applicants/{applicantId}")
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<ApplicantDto>> getApplicant(
      @PathVariable Integer applicantId) {

    ApplicantDto applicant = applicantService.getApplicant(applicantId);
    ApiResponse<ApplicantDto> response = ApiResponse.success(
        applicant,
        HttpStatus.OK.value(),
        "Applicant retrieved.",
        "Successfully retrieved applicant " + applicantId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/api/applicants/{applicantId}/status")
  @PreAuthorize("hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<ApplicantDto>> updateApplicantStatus(
      @PathVariable Integer applicantId,
      @Valid @RequestBody UpdateApplicantStatusDto dto) {

    ApplicantDto updatedApplicant = applicantService.updateApplicantStatus(applicantId, dto);
    ApiResponse<ApplicantDto> response = ApiResponse.success(
        updatedApplicant,
        HttpStatus.OK.value(),
        "Applicant status updated.",
        "Applicant " + applicantId + " status set to " + dto.getStatus());
    return ResponseEntity.ok(response);
  }
}