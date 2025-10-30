package com.konecta.recruitmentservice.dto;

import java.util.List;

import com.konecta.recruitmentservice.model.JobRequirement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobPostDto {
  private String title;
  private String description;
  private List<JobRequirement> requirements;
  private Integer requisitionId;
}