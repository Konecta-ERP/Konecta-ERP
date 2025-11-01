package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.OffboardingTask;
import com.konecta.employeeservice.model.enums.OffboardingStatus;
import lombok.Data;
import java.util.List;

@Data
public class OffboardingChecklistDto {
  private Integer id;
  private Integer employeeId;
  private OffboardingStatus status;
  private List<OffboardingTask> tasks;
}