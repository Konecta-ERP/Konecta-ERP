package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.JobRequirement;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobPostDto {
  private Integer id;
  private String title;
  private String description;
  private List<JobRequirement> requirements;
  private boolean active;
  private LocalDateTime postedAt;
  private Integer requisitionId;
}