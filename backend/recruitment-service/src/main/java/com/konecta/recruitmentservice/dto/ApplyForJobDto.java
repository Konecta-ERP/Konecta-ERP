package com.konecta.recruitmentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyForJobDto {

  @NotEmpty(message = "First name cannot be empty")
  @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
  private String firstName;

  @NotEmpty(message = "Last name cannot be empty")
  @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
  private String lastName;

  @NotEmpty(message = "Email cannot be empty")
  @Email(message = "Must be a valid email format")
  private String email;

  @NotEmpty(message = "CV URL cannot be empty")
  private String cvUrl;

  @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
  private String coverLetter;
}