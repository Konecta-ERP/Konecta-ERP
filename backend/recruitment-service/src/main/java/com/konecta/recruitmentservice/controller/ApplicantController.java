package com.konecta.recruitmentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/applicants/{applicantId}/download-cv")
    @PreAuthorize("hasAuthority('HR_EMP')")
    public ResponseEntity<byte[]> downloadApplicantCv(@PathVariable int applicantId) {
        byte[] submissionFile = applicantService.downloadCv(applicantId);
        String fileName = applicantService.getCvFileName(applicantId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(submissionFile);
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

    @DeleteMapping("/api/applicants/{applicantId}")
    @PreAuthorize("hasAuthority('HR_EMP')")
    public ResponseEntity<ApiResponse<Void>> deleteApplicant( @PathVariable Integer applicantId) {
        applicantService.deleteApplicant(applicantId);
        ApiResponse<Void> response = ApiResponse.success(
                null,
                HttpStatus.NO_CONTENT.value(),
                "Applicant deleted.",
                "Successfully deleted applicant " + applicantId);

        return ResponseEntity.status(204).body(response);
    }
}