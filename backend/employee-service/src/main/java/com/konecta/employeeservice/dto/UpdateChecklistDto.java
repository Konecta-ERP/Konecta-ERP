package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.OffboardingTask;
import com.konecta.employeeservice.model.enums.OffboardingStatus;
import lombok.Data;
import java.util.List;

@Data
public class UpdateChecklistDto {
  private List<OffboardingTask> tasks;
  private OffboardingStatus status;
}