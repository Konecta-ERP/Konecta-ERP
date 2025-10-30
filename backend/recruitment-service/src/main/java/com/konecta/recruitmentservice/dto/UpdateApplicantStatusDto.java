package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicantStatusDto {

  @NotNull(message = "Status is required")
  private ApplicantStatus status;
}