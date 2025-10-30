package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicantStatusDto {
  private ApplicantStatus status;
}