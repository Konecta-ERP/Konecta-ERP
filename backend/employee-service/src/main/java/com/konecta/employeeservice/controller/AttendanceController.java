package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.AttendanceRecordDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AttendanceController {

  private final AttendanceService attendanceService;

  @Autowired
  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

  @PostMapping("/employees/{id}/clock-in")
  public ResponseEntity<ApiResponse<AttendanceRecordDto>> clockIn(@PathVariable Integer id) {
    AttendanceRecordDto record = attendanceService.clockIn(id);
    ApiResponse<AttendanceRecordDto> response = ApiResponse.success(
        record,
        HttpStatus.OK.value(),
        "Clock-in recorded.",
        "Employee " + id + " clocked in.");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/employees/{id}/clock-out")
  public ResponseEntity<ApiResponse<AttendanceRecordDto>> clockOut(@PathVariable Integer id) {
    AttendanceRecordDto record = attendanceService.clockOut(id);
    ApiResponse<AttendanceRecordDto> response = ApiResponse.success(
        record,
        HttpStatus.OK.value(),
        "Clock-out recorded.",
        "Employee " + id + " clocked out.");
    return ResponseEntity.ok(response);
  }
}
