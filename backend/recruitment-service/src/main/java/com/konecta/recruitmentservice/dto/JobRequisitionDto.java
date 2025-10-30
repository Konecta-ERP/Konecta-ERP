package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobRequisitionDto {
  private Integer id;
  private String reason;
  private RequisitionPriority priority;
  private Integer openings;
  private RequisitionStatus status;
  private LocalDateTime createdAt;
  private Integer departmentId;
  private List<JobPostDto> jobPosts;
}