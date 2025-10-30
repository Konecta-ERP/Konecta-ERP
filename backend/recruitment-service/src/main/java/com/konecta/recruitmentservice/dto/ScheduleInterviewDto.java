package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.InterviewMode;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleInterviewDto {
  private InterviewMode mode;
  private LocalDateTime scheduledAt;
  private Integer interviewerId;
  private String feedback; // Optional initial feedback/notes
}