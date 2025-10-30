package com.konecta.employeeservice.dto;

import lombok.Data;

@Data
public class CreateGoalDto {
  private String title;
  private String description;
  private String target;
  private String cycle;
}
