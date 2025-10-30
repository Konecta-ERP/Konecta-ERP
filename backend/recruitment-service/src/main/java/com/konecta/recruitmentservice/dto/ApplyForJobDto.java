package com.konecta.recruitmentservice.dto;

import lombok.Data;

@Data
public class ApplyForJobDto {
  private String firstName;
  private String lastName;
  private String email;
  private String cvUrl;
  private String coverLetter;
}