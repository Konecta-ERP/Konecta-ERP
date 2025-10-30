package com.konecta.recruitmentservice.service;

import com.konecta.recruitmentservice.dto.ApplicantDto;
import com.konecta.recruitmentservice.dto.ApplyForJobDto;
import com.konecta.recruitmentservice.dto.UpdateApplicantStatusDto;
import com.konecta.recruitmentservice.entity.Applicant;
import com.konecta.recruitmentservice.entity.JobPost;
import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import com.konecta.recruitmentservice.repository.ApplicantRepository;
import com.konecta.recruitmentservice.repository.JobPostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicantService {

  private final ApplicantRepository applicantRepository;
  private final JobPostRepository jobPostRepository;

  @Autowired
  public ApplicantService(ApplicantRepository applicantRepository, JobPostRepository jobPostRepository) {
    this.applicantRepository = applicantRepository;
    this.jobPostRepository = jobPostRepository;
  }

  @Transactional
  public ApplicantDto applyForJob(Integer postId, ApplyForJobDto dto) {
    JobPost post = jobPostRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("JobPost not found with id: " + postId));

    if (!post.isActive()) {
      throw new IllegalStateException("This job post is not active and is not accepting applications.");
    }

    Applicant app = new Applicant();
    app.setJobPost(post);
    app.setFirstName(dto.getFirstName());
    app.setLastName(dto.getLastName());
    app.setEmail(dto.getEmail());
    app.setCvUrl(dto.getCvUrl());
    app.setCoverLetter(dto.getCoverLetter());
    app.setStatus(ApplicantStatus.APPLIED);

    Applicant savedApp = applicantRepository.save(app);
    return convertToDto(savedApp);
  }

  @Transactional(readOnly = true)
  public List<ApplicantDto> getApplicantsForPost(Integer postId) {
    if (!jobPostRepository.existsById(postId)) {
      throw new EntityNotFoundException("JobPost not found with id: " + postId);
    }
    return applicantRepository.findByJobPostId(postId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ApplicantDto getApplicant(Integer applicantId) {
    Applicant app = applicantRepository.findById(applicantId)
        .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));
    return convertToDto(app);
  }

  @Transactional
  public ApplicantDto updateApplicantStatus(Integer applicantId, UpdateApplicantStatusDto dto) {
    Applicant app = applicantRepository.findById(applicantId)
        .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

    app.setStatus(dto.getStatus());
    Applicant savedApp = applicantRepository.save(app);
    return convertToDto(savedApp);
  }

  private ApplicantDto convertToDto(Applicant app) {
    ApplicantDto dto = new ApplicantDto();
    dto.setId(app.getId());
    dto.setFirstName(app.getFirstName());
    dto.setLastName(app.getLastName());
    dto.setEmail(app.getEmail());
    dto.setCvUrl(app.getCvUrl());
    dto.setCoverLetter(app.getCoverLetter());
    dto.setStatus(app.getStatus());
    dto.setAppliedAt(app.getAppliedAt());
    dto.setJobPostId(app.getJobPost().getId());
    return dto;
  }
}