package com.konecta.employeeservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceRecordDto {
  private Integer id;
  private Integer employeeId;
  private LocalDateTime clockInTime;
  private LocalDateTime clockOutTime;
  private LocalDate date;
  private String status; // e.g., "CLOCKED_IN", "CLOCKED_OUT"
}
