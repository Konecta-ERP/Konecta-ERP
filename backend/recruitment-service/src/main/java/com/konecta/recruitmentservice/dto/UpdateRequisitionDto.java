package com.konecta.recruitmentservice.dto;

import java.util.Optional;

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
public class UpdateRequisitionDto {
  private Optional<String> reason = Optional.empty();
  private Optional<RequisitionPriority> priority = Optional.empty();
  private Optional<Integer> openings = Optional.empty();
  private Optional<RequisitionStatus> status = Optional.empty();
}