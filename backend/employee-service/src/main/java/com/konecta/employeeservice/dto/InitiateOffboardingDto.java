package com.konecta.employeeservice.dto;

import com.konecta.employeeservice.model.OffboardingTask;
import lombok.Data;
import java.util.List;

@Data
public class InitiateOffboardingDto {
  private List<OffboardingTask> tasks;
}