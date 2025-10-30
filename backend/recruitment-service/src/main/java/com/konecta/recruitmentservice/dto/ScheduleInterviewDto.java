package com.konecta.recruitmentservice.dto;

import java.time.LocalDateTime;

import com.konecta.recruitmentservice.model.enums.InterviewMode;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewDto {

  @NotNull(message = "Interview mode is required")
  private InterviewMode mode;

  @NotNull(message = "Interview scheduled time is required")
  @Future(message = "Interview must be scheduled in the future")
  private LocalDateTime scheduledAt;

  @NotNull(message = "Interviewer ID is required")
  private Integer interviewerId;

  @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
  private String feedback;
}