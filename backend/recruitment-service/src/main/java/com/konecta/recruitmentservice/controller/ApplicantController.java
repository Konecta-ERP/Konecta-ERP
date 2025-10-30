package com.konecta.recruitmentservice.controller;

import com.konecta.recruitmentservice.dto.ApplicantDto;
import com.konecta.recruitmentservice.dto.ApplyForJobDto;
import com.konecta.recruitmentservice.dto.UpdateApplicantStatusDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ApplicantController {

  private final ApplicantService applicantService;

  @Autowired
  public ApplicantController(ApplicantService applicantService) {
    this.applicantService = applicantService;
  }

  @GetMapping("/applicants/{applicantId}")
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

  @PatchMapping("/applicants/{applicantId}/status")
  public ResponseEntity<ApiResponse<ApplicantDto>> updateApplicantStatus(
      @PathVariable Integer applicantId,
      @RequestBody UpdateApplicantStatusDto dto) {

    ApplicantDto updatedApplicant = applicantService.updateApplicantStatus(applicantId, dto);
    ApiResponse<ApplicantDto> response = ApiResponse.success(
        updatedApplicant,
        HttpStatus.OK.value(),
        "Applicant status updated.",
        "Applicant " + applicantId + " status set to " + dto.getStatus());
    return ResponseEntity.ok(response);
  }
}