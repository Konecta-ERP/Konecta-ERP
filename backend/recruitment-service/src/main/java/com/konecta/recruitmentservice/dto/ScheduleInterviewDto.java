package com.konecta.recruitmentservice.dto;

import java.time.LocalDateTime;

import com.konecta.recruitmentservice.model.enums.InterviewMode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewDto {
  private InterviewMode mode;
  private LocalDateTime scheduledAt;
  private Integer interviewerId;
  private String feedback; // Optional initial feedback/notes
}