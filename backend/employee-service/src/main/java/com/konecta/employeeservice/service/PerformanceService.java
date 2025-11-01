package com.konecta.employeeservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konecta.employeeservice.dto.CreateFeedbackDto;
import com.konecta.employeeservice.dto.CreateGoalDto;
import com.konecta.employeeservice.dto.FeedbackDto;
import com.konecta.employeeservice.dto.GoalDto;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.entity.EmployeeGoal;
import com.konecta.employeeservice.entity.PerformanceFeedback;
import com.konecta.employeeservice.repository.EmployeeGoalRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.repository.PerformanceFeedbackRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PerformanceService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeGoalRepository goalRepository;
  private final PerformanceFeedbackRepository feedbackRepository;

  @Autowired
  public PerformanceService(EmployeeRepository employeeRepository,
      EmployeeGoalRepository goalRepository,
      PerformanceFeedbackRepository feedbackRepository) {
    this.employeeRepository = employeeRepository;
    this.goalRepository = goalRepository;
    this.feedbackRepository = feedbackRepository;
  }

  // --- GOALS LOGIC ---
  @Transactional
  public GoalDto assignGoal(Integer employeeId, CreateGoalDto dto) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    EmployeeGoal goal = new EmployeeGoal();
    goal.setEmployee(employee);
    goal.setTitle(dto.getTitle());
    goal.setDescription(dto.getDescription());
    goal.setTarget(dto.getTarget());
    goal.setCycle(dto.getCycle());

    EmployeeGoal savedGoal = goalRepository.save(goal);
    return convertToDto(savedGoal);
  }

  @Transactional(readOnly = true)
  public List<GoalDto> getGoalsForEmployee(Integer employeeId) {
    if (!employeeRepository.existsById(employeeId)) {
      throw new EntityNotFoundException("Employee not found with id: " + employeeId);
    }

    List<EmployeeGoal> goals = goalRepository.findByEmployeeId(employeeId);

    return goals.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteGoal(Integer goalId) {
    EmployeeGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new EntityNotFoundException("Goal not found with id: " + goalId));

    goalRepository.delete(goal);
  }

  // --- FEEDBACK LOGIC ---
  @Transactional
  public FeedbackDto giveFeedback(Integer recipientId, CreateFeedbackDto dto) {
    Employee recipient = employeeRepository.findById(recipientId)
        .orElseThrow(() -> new EntityNotFoundException("Recipient (Employee) not found with id: " + recipientId));

    Employee giver = employeeRepository.findById(dto.getGiverId())
        .orElseThrow(() -> new EntityNotFoundException("Giver (Employee) not found with id: " + dto.getGiverId()));

    PerformanceFeedback feedback = new PerformanceFeedback();
    feedback.setRecipient(recipient);
    feedback.setGiver(giver);
    feedback.setFeedback(dto.getFeedback());

    PerformanceFeedback savedFeedback = feedbackRepository.save(feedback);
    return convertToDto(savedFeedback);
  }

  @Transactional(readOnly = true)
  public List<FeedbackDto> getFeedbackForEmployee(Integer employeeId) {
    if (!employeeRepository.existsById(employeeId)) {
      throw new EntityNotFoundException("Employee (recipient) not found with id: " + employeeId);
    }

    List<PerformanceFeedback> feedbacks = feedbackRepository.findByRecipientId(employeeId);

    return feedbacks.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteFeedback(Integer feedbackId) {
    PerformanceFeedback feedback = feedbackRepository.findById(feedbackId)
        .orElseThrow(() -> new EntityNotFoundException("Feedback not found with id: " + feedbackId));

    feedbackRepository.delete(feedback);
  }

  // --- DTO Converters ---
  private GoalDto convertToDto(EmployeeGoal goal) {
    GoalDto dto = new GoalDto();
    dto.setId(goal.getId());
    dto.setTitle(goal.getTitle());
    dto.setDescription(goal.getDescription());
    dto.setTarget(goal.getTarget());
    dto.setCycle(goal.getCycle());
    dto.setEmployeeId(goal.getEmployee().getId());
    return dto;
  }

  private FeedbackDto convertToDto(PerformanceFeedback feedback) {
    FeedbackDto dto = new FeedbackDto();
    dto.setId(feedback.getId());
    dto.setFeedback(feedback.getFeedback());
    dto.setCreatedAt(feedback.getCreatedAt());
    dto.setRecipientId(feedback.getRecipient().getId());
    dto.setGiverId(feedback.getGiver().getId());
    return dto;
  }
}
