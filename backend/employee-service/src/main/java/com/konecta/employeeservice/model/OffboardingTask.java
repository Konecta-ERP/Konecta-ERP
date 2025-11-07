package com.konecta.employeeservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffboardingTask {
  private String task;
  private boolean complete;
}
