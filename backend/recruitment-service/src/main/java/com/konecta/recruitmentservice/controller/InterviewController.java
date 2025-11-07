package com.konecta.recruitmentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.konecta.recruitmentservice.dto.InterviewDto;
import com.konecta.recruitmentservice.dto.ScheduleInterviewDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.service.InterviewService;

import jakarta.validation.Valid;

@RestController
public class InterviewController {

  private final InterviewService interviewService;

  @Autowired
  public InterviewController(InterviewService interviewService) {
    this.interviewService = interviewService;
  }

  @PostMapping("/applicants/{applicantId}/interviews")
  public ResponseEntity<ApiResponse<InterviewDto>> scheduleInterview(
      @PathVariable Integer applicantId,
      @Valid @RequestBody ScheduleInterviewDto dto) {

    InterviewDto newInterview = interviewService.scheduleInterview(applicantId, dto);
    ApiResponse<InterviewDto> response = ApiResponse.success(
        newInterview,
        HttpStatus.CREATED.value(),
        "Interview scheduled successfully.",
        "Interview created with id " + newInterview.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}