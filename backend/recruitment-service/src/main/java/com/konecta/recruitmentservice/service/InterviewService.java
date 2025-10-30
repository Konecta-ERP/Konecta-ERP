package com.konecta.recruitmentservice.service;

import com.konecta.recruitmentservice.dto.InterviewDto;
import com.konecta.recruitmentservice.dto.ScheduleInterviewDto;
import com.konecta.recruitmentservice.entity.Applicant;
import com.konecta.recruitmentservice.entity.Interview;
import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import com.konecta.recruitmentservice.model.enums.InterviewStatus;
import com.konecta.recruitmentservice.repository.ApplicantRepository;
import com.konecta.recruitmentservice.repository.InterviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {

  private final InterviewRepository interviewRepository;
  private final ApplicantRepository applicantRepository;

  @Autowired
  public InterviewService(InterviewRepository interviewRepository, ApplicantRepository applicantRepository) {
    this.interviewRepository = interviewRepository;
    this.applicantRepository = applicantRepository;
  }

  @Transactional
  public InterviewDto scheduleInterview(Integer applicantId, ScheduleInterviewDto dto) {
    Applicant app = applicantRepository.findById(applicantId)
        .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

    Interview interview = new Interview();
    interview.setApplicant(app);
    interview.setMode(dto.getMode());
    interview.setScheduledAt(dto.getScheduledAt());
    interview.setInterviewerId(dto.getInterviewerId());
    interview.setFeedback(dto.getFeedback());
    interview.setStatus(InterviewStatus.PENDING);

    Interview savedInterview = interviewRepository.save(interview);

    // Update applicant status
    app.setStatus(ApplicantStatus.INTERVIEW);
    applicantRepository.save(app);

    return convertToDto(savedInterview);
  }

  private InterviewDto convertToDto(Interview interview) {
    InterviewDto dto = new InterviewDto();
    dto.setId(interview.getId());
    dto.setFeedback(interview.getFeedback());
    dto.setStatus(interview.getStatus());
    dto.setMode(interview.getMode());
    dto.setScheduledAt(interview.getScheduledAt());
    dto.setInterviewerId(interview.getInterviewerId());
    dto.setApplicantId(interview.getApplicant().getId());
    return dto;
  }
}