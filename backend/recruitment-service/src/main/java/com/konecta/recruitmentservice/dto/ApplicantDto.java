package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicantDto {
  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String cvUrl;
  private String coverLetter;
  private ApplicantStatus status;
  private LocalDateTime appliedAt;
  private Integer jobPostId;
  // add interviews and offers here later
}