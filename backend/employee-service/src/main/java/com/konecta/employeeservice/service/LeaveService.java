package com.konecta.employeeservice.service;

import com.konecta.employeeservice.dto.CreateLeaveRequestDto;
import com.konecta.employeeservice.dto.LeaveBalanceDto;
import com.konecta.employeeservice.dto.LeaveRequestDto;
import com.konecta.employeeservice.dto.UpdateLeaveStatusDto;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.entity.LeaveRequest;
import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.repository.LeaveRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

  private final LeaveRequestRepository leaveRequestRepository;
  private final EmployeeRepository employeeRepository;
  private static final int TOTAL_VACATION_DAYS = 20; // Example total

  @Autowired
  public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public LeaveRequestDto createLeaveRequest(Integer employeeId, CreateLeaveRequestDto dto) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    LeaveRequest request = new LeaveRequest();
    request.setEmployee(employee);
    request.setRequestType(dto.getRequestType());
    request.setReason(dto.getReason());
    request.setStartDate(dto.getStartDate());
    request.setEndDate(dto.getEndDate());
    request.setStatus(RequestStatus.PENDING); // Default status

    LeaveRequest savedRequest = leaveRequestRepository.save(request);
    return convertToDto(savedRequest);
  }

  @Transactional
  public LeaveRequestDto updateRequestStatus(Integer requestId, UpdateLeaveStatusDto dto) {
    LeaveRequest request = leaveRequestRepository.findById(requestId)
        .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + requestId));

    if (request.getStatus() == RequestStatus.REJECTED) {
      throw new IllegalStateException("Cannot change status of a rejected request.");
    }

    if (request.getStatus() == dto.getStatus()) {
      throw new IllegalStateException("Leave request status is already " + dto.getStatus());
    }

    request.setStatus(dto.getStatus());

    LeaveRequest updatedRequest = leaveRequestRepository.save(request);
    return convertToDto(updatedRequest);
  }

  @Transactional(readOnly = true)
  public LeaveBalanceDto getLeaveBalance(Integer employeeId) {
    if (!employeeRepository.existsById(employeeId)) {
      throw new EntityNotFoundException("Employee not found with id: " + employeeId);
    }

    // Get all APPROVED vacation requests
    List<LeaveRequest> approvedVacations = leaveRequestRepository
        .findByEmployeeIdAndStatusAndRequestType(
            employeeId,
            RequestStatus.APPROVED,
            RequestType.VACATION);

    // Get all APPROVED sick leave requests
    List<LeaveRequest> approvedSickLeaves = leaveRequestRepository
        .findByEmployeeIdAndStatusAndRequestType(
            employeeId,
            RequestStatus.APPROVED,
            RequestType.SICK);

    // Calculate the total days taken for each type
    long vacationTaken = approvedVacations.stream()
        .mapToLong(this::calculateLeaveDurationInDays)
        .sum();

    long sickTaken = approvedSickLeaves.stream()
        .mapToLong(this::calculateLeaveDurationInDays)
        .sum();

    return new LeaveBalanceDto(
        employeeId,
        (int) vacationTaken,
        TOTAL_VACATION_DAYS - (int) vacationTaken,
        (int) sickTaken);
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getRequestsForEmployee(Integer employeeId) {
    if (!employeeRepository.existsById(employeeId)) {
      throw new EntityNotFoundException("Employee not found with id: " + employeeId);
    }

    return leaveRequestRepository.findByEmployeeId(employeeId).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestDto> getRequestsForEmployeeInRange(Integer employeeId, java.time.LocalDate start,
      java.time.LocalDate end) {
    // Reuse existing method which validates employee existence
    List<LeaveRequestDto> all = getRequestsForEmployee(employeeId);

    return all.stream()
        .filter(r -> {
          if (r.getStartDate() == null || r.getEndDate() == null) return false;
          // keep requests that overlap [start, end]
          return !(r.getEndDate().isBefore(start) || r.getStartDate().isAfter(end));
        })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public LeaveRequestDto getRequestById(Integer requestId) {
    LeaveRequest request = leaveRequestRepository.findById(requestId)
        .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + requestId));
    return convertToDto(request);
  }

  @Transactional
  public void deleteLeaveRequest(Integer requestId) {
    LeaveRequest request = leaveRequestRepository.findById(requestId)
        .orElseThrow(() -> new EntityNotFoundException("Leave request not found with id: " + requestId));

    if (request.getStatus() != RequestStatus.PENDING) {
      throw new IllegalStateException("Cannot delete a request that is not pending.");
    }

    leaveRequestRepository.delete(request);
  }

  /**
   * Calculates the duration of a leave request in days, inclusive.
   * E.g., A request from Oct 10 to Oct 12 is 3 days.
   * E.g., A request from Oct 10 to Oct 10 is 1 day.
   */
  private long calculateLeaveDurationInDays(LeaveRequest request) {
    // ChronoUnit.DAYS.between is exclusive of the end date.
    // Adding 1 makes the calculation inclusive.
    return ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
  }

  // --- DTO Converter ---
  private LeaveRequestDto convertToDto(LeaveRequest request) {
    LeaveRequestDto dto = new LeaveRequestDto();
    dto.setId(request.getId());
    dto.setRequestType(request.getRequestType());
    dto.setReason(request.getReason());
    dto.setStartDate(request.getStartDate());
    dto.setEndDate(request.getEndDate());
    dto.setStatus(request.getStatus());
    dto.setEmployeeId(request.getEmployee().getId());

    // User details are managed by Identity service; enrich later if needed
    dto.setEmployeeName(null);
    return dto;
  }
}
