package com.konecta.recruitmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyForJobDto {
  private String firstName;
  private String lastName;
  private String email;
  private String cvUrl;
  private String coverLetter;
}