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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {

  private final InterviewRepository interviewRepository;
  private final ApplicantRepository applicantRepository;
  private final RestTemplate restTemplate;

  @Autowired
  public InterviewService(InterviewRepository interviewRepository, ApplicantRepository applicantRepository,
      RestTemplate restTemplate) {
    this.interviewRepository = interviewRepository;
    this.applicantRepository = applicantRepository;
    this.restTemplate = restTemplate;
  }

  @Transactional
  public InterviewDto scheduleInterview(Integer applicantId, ScheduleInterviewDto dto) {
    Applicant app = applicantRepository.findById(applicantId)
        .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

    validateInterviewerExists(dto.getInterviewerId());

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

  private void validateInterviewerExists(Integer interviewerId) {
    String url = "http://employee-service/employees/" + interviewerId;
    try {
      // Forward caller's JWT if present so employee-service can apply its own access
      // rules
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String bearerToken = null;
      if (auth instanceof JwtAuthenticationToken) {
        bearerToken = ((JwtAuthenticationToken) auth).getToken().getTokenValue();
      } else if (auth != null && auth.getCredentials() instanceof String) {
        bearerToken = (String) auth.getCredentials();
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
      if (bearerToken != null && !bearerToken.isBlank()) {
        headers.setBearerAuth(bearerToken);
      }

      HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
      ResponseEntity<?> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, java.util.Map.class);

      int status = resp.getStatusCode().value();
      if (status == 404) {
        throw new EntityNotFoundException("Interviewer not found with id: " + interviewerId);
      }
      // treat 2xx as ok; treat 401/403 as "exists but not authorized" (still a valid
      // id)
      if ((status >= 200 && status < 300) || status == 401 || status == 403) {
        return;
      }

      throw new IllegalStateException("Unexpected response from employee service: " + status);
    } catch (HttpClientErrorException.NotFound e) {
      throw new EntityNotFoundException("Interviewer not found with id: " + interviewerId);
    } catch (HttpClientErrorException e) {
      // 401/403 will reach here with a status code; treat as exists
      if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
        return;
      }
      throw new IllegalStateException("Employee service error: " + e.getStatusCode(), e);
    } catch (RestClientException e) {
      throw new IllegalStateException("Failed to call employee service: " + e.getMessage(), e);
    }
  }
}