package com.konecta.recruitmentservice.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRequirement {

  @NotEmpty(message = "Requirement text cannot be empty")
  private String text;

  private boolean mandatory;
}