package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import lombok.Data;

@Data
public class UpdateApplicantStatusDto {
  private ApplicantStatus status;
}