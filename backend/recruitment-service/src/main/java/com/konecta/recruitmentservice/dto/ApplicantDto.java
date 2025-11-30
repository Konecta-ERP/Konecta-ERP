package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDto {
  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String cvFileName;
  private String cvFileType;
  private String cvFilePath;
  private String coverLetter;
  private ApplicantStatus status;
  private LocalDateTime appliedAt;
  private Integer jobPostId;
  // add interviews and offers here later
}