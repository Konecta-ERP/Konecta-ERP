package com.konecta.employeeservice.dto;

import lombok.Data;

@Data
public class GoalDto {
  private Integer id;
  private String title;
  private String description;
  private String target;
  private String cycle;
  private Integer employeeId;
}
