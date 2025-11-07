package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.CreateFeedbackDto;
import com.konecta.employeeservice.dto.CreateGoalDto;
import com.konecta.employeeservice.dto.FeedbackDto;
import com.konecta.employeeservice.dto.GoalDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.PerformanceService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PerformanceController {

  private final PerformanceService performanceService;

  @Autowired
  public PerformanceController(PerformanceService performanceService) {
    this.performanceService = performanceService;
  }

  @PostMapping("/employees/{id}/goals")
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
  public ResponseEntity<ApiResponse<List<GoalDto>>> getEmployeeGoals(@PathVariable(name = "id") Integer id) {
    List<GoalDto> goals = performanceService.getGoalsForEmployee(id);
    ApiResponse<List<GoalDto>> response = ApiResponse.success(
        goals,
        HttpStatus.OK.value(),
        "Goals retrieved.",
        "Retrieved " + goals.size() + " goals for employee " + id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/employees/{id}/feedback")
  public ResponseEntity<ApiResponse<List<FeedbackDto>>> getEmployeeFeedback(@PathVariable(name = "id") Integer id) {
    List<FeedbackDto> feedbacks = performanceService.getFeedbackForEmployee(id);
    ApiResponse<List<FeedbackDto>> response = ApiResponse.success(
        feedbacks,
        HttpStatus.OK.value(),
        "Feedback retrieved.",
        "Retrieved " + feedbacks.size() + " feedback entries for employee " + id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/goals/{id}")
  public ResponseEntity<ApiResponse<Object>> deleteGoal(@PathVariable(name = "id") Integer id) {
    performanceService.deleteGoal(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Goal deleted.",
        "Goal " + id + " deleted.");
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/feedback/{id}")
  public ResponseEntity<ApiResponse<Object>> deleteFeedback(@PathVariable(name = "id") Integer id) {
    performanceService.deleteFeedback(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Feedback deleted.",
        "Feedback " + id + " deleted.");
    return ResponseEntity.ok(response);
  }
}
