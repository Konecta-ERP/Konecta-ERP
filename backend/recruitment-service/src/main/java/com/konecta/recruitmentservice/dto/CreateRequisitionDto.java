package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import lombok.Data;

@Data
public class CreateRequisitionDto {
  private String reason;
  private RequisitionPriority priority;
  private Integer openings;
  private Integer departmentId;
}