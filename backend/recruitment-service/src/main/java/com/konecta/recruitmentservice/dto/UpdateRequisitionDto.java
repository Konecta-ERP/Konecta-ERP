package com.konecta.recruitmentservice.dto;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import lombok.Data;

import java.util.Optional;

@Data
public class UpdateRequisitionDto {
  private Optional<String> reason = Optional.empty();
  private Optional<RequisitionPriority> priority = Optional.empty();
  private Optional<Integer> openings = Optional.empty();
  private Optional<RequisitionStatus> status = Optional.empty();
}