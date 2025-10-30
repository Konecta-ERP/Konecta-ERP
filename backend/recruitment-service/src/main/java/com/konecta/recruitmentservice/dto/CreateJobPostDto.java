package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.JobRequirement;
import lombok.Data;
import java.util.List;

@Data
public class CreateJobPostDto {
  private String title;
  private String description;
  private List<JobRequirement> requirements;
  private Integer requisitionId;
}