package com.konecta.recruitmentservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequirement {
  private String text;
  private boolean mandatory;
}