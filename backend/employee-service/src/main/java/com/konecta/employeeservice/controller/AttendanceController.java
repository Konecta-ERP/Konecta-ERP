package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.AttendanceRecordDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.AttendanceService;
import com.konecta.employeeservice.service.EmployeeService;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.NoSuchElementException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

@RestController
public class AttendanceController {

  private final AttendanceService attendanceService;
  private final EmployeeService employeeService;

  @Autowired
  public AttendanceController(AttendanceService attendanceService, EmployeeService employeeService) {
    this.attendanceService = attendanceService;
    this.employeeService = employeeService;
  }

  @PostMapping("/api/employees/{id}/clock-in")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<AttendanceRecordDto>> clockIn(@PathVariable(name = "id") Integer id,
      Authentication authentication) {
    // Only allow the employee themselves to clock in
    String jwtUserId = extractUserIdClaim(authentication);
    EmployeeDetailsDto emp;
    try {
      emp = employeeService.getEmployeeDetailsById(id);
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      // Treat "not found" the same as unauthorized to avoid user enumeration
      throw new AccessDeniedException("Access denied: can only clock in for your own employee record");
    }

    String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
    if (jwtUserId == null || !jwtUserId.equals(employeeUserId)) {
      throw new AccessDeniedException("Access denied: can only clock in for your own employee record");
    }

    AttendanceRecordDto record = attendanceService.clockIn(id);
    ApiResponse<AttendanceRecordDto> response = ApiResponse.success(
        record,
        HttpStatus.OK.value(),
        "Clock-in recorded.",
        "Employee " + id + " clocked in.");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/api/employees/{id}/clock-out")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<AttendanceRecordDto>> clockOut(@PathVariable(name = "id") Integer id,
      Authentication authentication) {
    // Only allow the employee themselves to clock out
    String jwtUserId = extractUserIdClaim(authentication);
    EmployeeDetailsDto emp;
    try {
      emp = employeeService.getEmployeeDetailsById(id);
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      // Treat "not found" the same as unauthorized to avoid user enumeration
      throw new AccessDeniedException("Access denied: can only clock out for your own employee record");
    }

    String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
    if (jwtUserId == null || !jwtUserId.equals(employeeUserId)) {
      throw new AccessDeniedException("Access denied: can only clock out for your own employee record");
    }

    AttendanceRecordDto record = attendanceService.clockOut(id);
    ApiResponse<AttendanceRecordDto> response = ApiResponse.success(
        record,
        HttpStatus.OK.value(),
        "Clock-out recorded.",
        "Employee " + id + " clocked out.");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/employees/{id}/attendance/latest")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<AttendanceRecordDto>> getLatestAttendance(@PathVariable(name = "id") Integer id,
      Authentication authentication) {
    // Only the employee themselves may call this
    String jwtUserId = extractUserIdClaim(authentication);
    EmployeeDetailsDto emp;
    try {
      emp = employeeService.getEmployeeDetailsById(id);
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      throw new AccessDeniedException("Access denied: can only view your own attendance record");
    }

    String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
    if (jwtUserId == null || !jwtUserId.equals(employeeUserId)) {
      throw new AccessDeniedException("Access denied: can only view your own attendance record");
    }

    AttendanceRecordDto latest = attendanceService.getLatestAttendanceRecord(id);

    ApiResponse<AttendanceRecordDto> response = ApiResponse.success(
        latest,
        HttpStatus.OK.value(),
        latest == null ? "No attendance records." : "Latest attendance retrieved.",
        "Latest attendance for employee " + id);
    return ResponseEntity.ok(response);
  }

  private String extractUserIdClaim(Authentication authentication) {
    if (authentication == null)
      return null;
    if (authentication instanceof JwtAuthenticationToken) {
      Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
      return jwt.getClaimAsString("userId");
    } else if (authentication.getPrincipal() instanceof Jwt) {
      Jwt jwt = (Jwt) authentication.getPrincipal();
      return jwt.getClaimAsString("userId");
    }
    return null;
  }
}
