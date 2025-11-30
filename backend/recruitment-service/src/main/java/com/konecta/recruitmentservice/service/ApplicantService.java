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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicantService {

  private final ApplicantRepository applicantRepository;
  private final JobPostRepository jobPostRepository;
    @Value("${file.upload.base-path.cv}")
    private String UPLOAD_DIR ;

  @Autowired
  public ApplicantService(ApplicantRepository applicantRepository, JobPostRepository jobPostRepository) {
    this.applicantRepository = applicantRepository;
    this.jobPostRepository = jobPostRepository;
  }

  @Transactional
  public ApplicantDto applyForJob(Integer postId, ApplyForJobDto dto, MultipartFile file) {
    JobPost post = jobPostRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("JobPost not found with id: " + postId));

    if (!post.isActive()) {
      throw new IllegalStateException("This job post is not active and is not accepting applications.");
    }
    String filePath = UPLOAD_DIR + file.getOriginalFilename();

    Applicant app = new Applicant();
    app.setJobPost(post);
    app.setFirstName(dto.getFirstName());
    app.setLastName(dto.getLastName());
    app.setEmail(dto.getEmail());
    app.setCvFileName(file.getOriginalFilename());
    app.setCvFileType(file.getContentType());
    app.setCvFilePath(filePath);
    app.setCoverLetter(dto.getCoverLetter());
    app.setStatus(ApplicantStatus.APPLIED);
      try {
          file.transferTo(new File(filePath));
      } catch (IOException e) {
          throw new IllegalStateException("Unable to store the file at " + filePath);
      }
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

    public byte[] downloadCv(int applicantId) {
        Applicant app = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

        String filePath = app.getCvFilePath();
        try {
            return Files.readAllBytes(new File(filePath).toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load the file from " + filePath);
        }
    }

    public String getCvFileName(int applicantId) {
        Applicant app = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));
        return app.getCvFileName();
    }

    @Transactional
    public void deleteApplicant(Integer applicantId) {
        Applicant app = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));
        if (app.getCvFilePath() != null) {
            File cvFile = new File(app.getCvFilePath());
            if (cvFile.exists()) {
                boolean deleted = cvFile.delete();
                if (!deleted) {
                    System.err.println("Warning: Could not delete CV file at: " + app.getCvFilePath());
                }
            }
        }
        applicantRepository.delete(app);
    }

  private ApplicantDto convertToDto(Applicant app) {
    ApplicantDto dto = new ApplicantDto();
    dto.setId(app.getId());
    dto.setFirstName(app.getFirstName());
    dto.setLastName(app.getLastName());
    dto.setEmail(app.getEmail());
    dto.setCvFilePath(app.getCvFilePath());
    dto.setCvFileType(app.getCvFileType());
    dto.setCvFileName(app.getCvFileName());
    dto.setCoverLetter(app.getCoverLetter());
    dto.setStatus(app.getStatus());
    dto.setAppliedAt(app.getAppliedAt());
    dto.setJobPostId(app.getJobPost().getId());
    return dto;
  }
}