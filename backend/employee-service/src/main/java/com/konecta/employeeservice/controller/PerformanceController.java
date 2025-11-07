package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.CreateFeedbackDto;
import com.konecta.employeeservice.dto.CreateGoalDto;
import com.konecta.employeeservice.dto.FeedbackDto;
import com.konecta.employeeservice.dto.GoalDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.PerformanceService;
import com.konecta.employeeservice.service.EmployeeService;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Objects;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PerformanceController {

  private final PerformanceService performanceService;
  private final EmployeeService employeeService;

  @Autowired
  public PerformanceController(PerformanceService performanceService, EmployeeService employeeService) {
    this.performanceService = performanceService;
    this.employeeService = employeeService;
  }

  @PostMapping("/employees/{id}/goals")
  @PreAuthorize("hasAuthority('MANAGER')")
  public ResponseEntity<ApiResponse<GoalDto>> assignGoalToEmployee(
      @PathVariable(name = "id") Integer id,
      @RequestBody CreateGoalDto goalDto) {

    GoalDto newGoal = performanceService.assignGoal(id, goalDto);
    ApiResponse<GoalDto> response = ApiResponse.success(
        newGoal,
        HttpStatus.CREATED.value(),
        "Goal assigned.",
        "Assigned goal with id " + newGoal.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/employees/{id}/feedback")
  @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('HR_EMP')")
  public ResponseEntity<ApiResponse<FeedbackDto>> giveFeedbackToEmployee(
      @PathVariable(name = "id") Integer id,
      @RequestBody CreateFeedbackDto feedbackDto) {

    FeedbackDto newFeedback = performanceService.giveFeedback(id, feedbackDto);
    ApiResponse<FeedbackDto> response = ApiResponse.success(
        newFeedback,
        HttpStatus.CREATED.value(),
        "Feedback submitted.",
        "Created feedback with id " + newFeedback.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/employees/{id}/goals")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<GoalDto>>> getEmployeeGoals(@PathVariable(name = "id") Integer id,
      Authentication authentication) {

    // Allow managers to access any employee
    boolean isManager = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()));

    if (!isManager) {
      // Non-manager users can only fetch their own goals
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
      }

      EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(id);
      String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
      if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
        throw new AccessDeniedException("Access denied: can only view your own goals");
      }
    }

    List<GoalDto> goals = performanceService.getGoalsForEmployee(id);
    ApiResponse<List<GoalDto>> response = ApiResponse.success(
        goals,
        HttpStatus.OK.value(),
        "Goals retrieved.",
        "Retrieved " + goals.size() + " goals for employee " + id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/employees/{id}/feedback")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<FeedbackDto>>> getEmployeeFeedback(@PathVariable(name = "id") Integer id,
      Authentication authentication) {

    // Allow managers to access any employee
    boolean isManager = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()));

    if (!isManager) {
      // Non-manager users can only fetch their own feedback
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
      }

      EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(id);
      String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
      if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
        throw new AccessDeniedException("Access denied: can only view your own goals");
      }
    }

    List<FeedbackDto> feedbacks = performanceService.getFeedbackForEmployee(id);
    ApiResponse<List<FeedbackDto>> response = ApiResponse.success(
        feedbacks,
        HttpStatus.OK.value(),
        "Feedback retrieved.",
        "Retrieved " + feedbacks.size() + " feedback entries for employee " + id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/goals/{id}")
  @PreAuthorize("hasAuthority('MANAGER')")
  public ResponseEntity<ApiResponse<Object>> deleteGoal(@PathVariable(name = "id") Integer id) {
    performanceService.deleteGoal(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Goal deleted.",
        "Goal " + id + " deleted.");
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/feedback/{id}")
  @PreAuthorize("hasAuthority('MANAGER')")
  public ResponseEntity<ApiResponse<Object>> deleteFeedback(@PathVariable(name = "id") Integer id) {
    performanceService.deleteFeedback(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Feedback deleted.",
        "Feedback " + id + " deleted.");
    return ResponseEntity.ok(response);
  }
}
