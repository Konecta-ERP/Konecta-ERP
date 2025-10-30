package com.konecta.recruitmentservice.dto;

import java.util.List;

import com.konecta.recruitmentservice.model.JobRequirement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateJobPostDto {

  @NotEmpty(message = "Title cannot be empty")
  @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
  private String title;

  @NotEmpty(message = "Description cannot be empty")
  @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
  private String description;

  @Valid
  private List<JobRequirement> requirements;

  @NotNull(message = "Requisition ID is required")
  private Integer requisitionId;
}