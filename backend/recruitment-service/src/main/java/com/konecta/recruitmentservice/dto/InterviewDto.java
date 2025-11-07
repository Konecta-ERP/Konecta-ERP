package com.konecta.recruitmentservice.dto;

import java.time.LocalDateTime;

import com.konecta.recruitmentservice.model.enums.InterviewMode;
import com.konecta.recruitmentservice.model.enums.InterviewStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDto {
  private Integer id;
  private String feedback;
  private InterviewStatus status;
  private InterviewMode mode;
  private LocalDateTime scheduledAt;
  private Integer interviewerId;
  private Integer applicantId;
}