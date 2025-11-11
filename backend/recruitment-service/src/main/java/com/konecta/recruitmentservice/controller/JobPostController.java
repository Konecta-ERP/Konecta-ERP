package com.konecta.recruitmentservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konecta.recruitmentservice.dto.ApplicantDto;
import com.konecta.recruitmentservice.dto.ApplyForJobDto;
import com.konecta.recruitmentservice.dto.CreateJobPostDto;
import com.konecta.recruitmentservice.dto.JobPostDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.service.ApplicantService;
import com.konecta.recruitmentservice.service.JobPostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/job-posts")
public class JobPostController {

  private final JobPostService jobPostService;
  private final ApplicantService applicantService;

  @Autowired
  public JobPostController(JobPostService jobPostService, ApplicantService applicantService) {
    this.jobPostService = jobPostService;
    this.applicantService = applicantService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ASSOCIATE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<JobPostDto>> createJobPost(
      @Valid @RequestBody CreateJobPostDto dto) {
    JobPostDto newPost = jobPostService.createJobPost(dto);
    ApiResponse<JobPostDto> response = ApiResponse.success(
        newPost,
        HttpStatus.CREATED.value(),
        "Job post created successfully.",
        "Job post created with id " + newPost.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<JobPostDto>> getJobPost(
      @PathVariable Integer id) {

    JobPostDto post = jobPostService.getJobPost(id);
    ApiResponse<JobPostDto> response = ApiResponse.success(
        post,
        HttpStatus.OK.value(),
        "Job post retrieved.",
        "Successfully retrieved job post id " + id);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/active")
  @PreAuthorize("hasAuthority('ASSOCIATE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<JobPostDto>> setJobPostActive(
      @PathVariable Integer id,
      @RequestBody Map<String, Boolean> activeUpdate) {

    Boolean active = activeUpdate.get("active");
    if (active == null) {
      throw new IllegalArgumentException("Request body must contain 'active' field.");
    }

    JobPostDto updatedPost = jobPostService.activateJobPost(id, active);
    ApiResponse<JobPostDto> response = ApiResponse.success(
        updatedPost,
        HttpStatus.OK.value(),
        "Job post activation status updated.",
        "Job post " + id + " active status set to " + active);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<JobPostDto>>> searchJobPosts(
      @RequestParam(name = "position", required = false) String position,
      @RequestParam(name = "departmentId", required = false) Integer departmentId,
      @RequestParam(name = "active", required = false) Boolean active) {

    List<JobPostDto> posts = jobPostService.searchJobPosts(position, departmentId, active);
    ApiResponse<List<JobPostDto>> response = ApiResponse.success(
        posts,
        HttpStatus.OK.value(),
        "Job posts retrieved successfully.",
        "Found " + posts.size() + " job posts.");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{postId}/apply")
  public ResponseEntity<ApiResponse<ApplicantDto>> applyForJob(
      @PathVariable Integer postId,
      @Valid @RequestBody ApplyForJobDto dto) {

    ApplicantDto newApplicant = applicantService.applyForJob(postId, dto);
    ApiResponse<ApplicantDto> response = ApiResponse.success(
        newApplicant,
        HttpStatus.CREATED.value(),
        "Application submitted successfully.",
        "Applicant created with id " + newApplicant.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{postId}/applicants")
  @PreAuthorize("hasAuthority('ASSOCIATE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<List<ApplicantDto>>> getApplicantsForPost(
      @PathVariable Integer postId) {

    List<ApplicantDto> applicants = applicantService.getApplicantsForPost(postId);
    ApiResponse<List<ApplicantDto>> response = ApiResponse.success(
        applicants,
        HttpStatus.OK.value(),
        "Applicants retrieved successfully.",
        "Found " + applicants.size() + " applicants.");
    return ResponseEntity.ok(response);
  }
}