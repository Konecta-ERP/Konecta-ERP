package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.InterviewMode;
import com.konecta.recruitmentservice.model.enums.InterviewStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewDto {
  private Integer id;
  private String feedback;
  private InterviewStatus status;
  private InterviewMode mode;
  private LocalDateTime scheduledAt;
  private Integer interviewerId;
  private Integer applicantId;
}