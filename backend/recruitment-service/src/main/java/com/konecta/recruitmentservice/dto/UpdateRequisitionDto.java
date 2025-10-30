package com.konecta.recruitmentservice.dto;

import java.util.Optional;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequisitionDto {

  @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
  private Optional<String> reason = Optional.empty();

  private Optional<RequisitionPriority> priority = Optional.empty();

  @Min(value = 1, message = "Number of openings must be at least 1")
  private Optional<Integer> openings = Optional.empty();

  private Optional<RequisitionStatus> status = Optional.empty();
}