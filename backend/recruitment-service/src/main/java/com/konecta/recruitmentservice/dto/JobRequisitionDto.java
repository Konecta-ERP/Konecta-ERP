package com.konecta.recruitmentservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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