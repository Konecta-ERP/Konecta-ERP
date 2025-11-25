package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.CreateLeaveRequestDto;
import com.konecta.employeeservice.dto.LeaveBalanceDto;
import com.konecta.employeeservice.dto.LeaveRequestDto;
import com.konecta.employeeservice.dto.UpdateLeaveStatusDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.LeaveService;
import com.konecta.employeeservice.service.EmployeeService;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Objects;
import jakarta.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LeaveController {

  private final LeaveService leaveService;
  private final EmployeeService employeeService;

  @Autowired
  public LeaveController(LeaveService leaveService, EmployeeService employeeService) {
    this.leaveService = leaveService;
    this.employeeService = employeeService;
  }

  @PostMapping("/api/employees/{employeeId}/leave-requests")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> submitLeaveRequest(
      @PathVariable(name = "employeeId") Integer employeeId,
      @RequestBody CreateLeaveRequestDto dto,
      Authentication authentication) {
    // Only the employee themselves can submit a leave request
    String jwtUserId = null;
    if (authentication instanceof JwtAuthenticationToken) {
      jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
    } else if (authentication.getPrincipal() instanceof Jwt) {
      jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
    }

    try {
      EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(employeeId);
      String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
      if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
        throw new AccessDeniedException("Access denied: can only submit leave for your own employee record");
      }
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      // Treat not-found as access denied to avoid enumeration
      throw new AccessDeniedException("Access denied: can only submit leave for your own employee record");
    }

    LeaveRequestDto newRequest = leaveService.createLeaveRequest(employeeId, dto);

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        newRequest,
        HttpStatus.CREATED.value(),
        "Leave request submitted successfully.",
        "LeaveRequest created with id " + newRequest.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PatchMapping("/api/leave-requests/{requestId}/status")
  @PreAuthorize("hasAuthority('MANAGER')")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> updateLeaveRequestStatus(
      @PathVariable(name = "requestId") Integer requestId,
      @RequestBody UpdateLeaveStatusDto dto) {
    LeaveRequestDto updatedRequest = leaveService.updateRequestStatus(requestId, dto);

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        updatedRequest,
        HttpStatus.OK.value(),
        "Request status updated.",
        "LeaveRequest " + requestId + " status updated to " + dto.getStatus());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/employees/{employeeId}/leave-balance")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<LeaveBalanceDto>> getEmployeeLeaveBalance(
      @PathVariable(name = "employeeId") Integer employeeId,
      Authentication authentication) {
    // Allow manager or the employee themselves
    boolean isManager = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()));

    if (!isManager) {
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
      }

      try {
        EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(employeeId);
        String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
        if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
          throw new AccessDeniedException("Access denied: can only view your own leave balance");
        }
      } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
          | IllegalArgumentException ex) {
        throw new AccessDeniedException("Access denied: can only view your own leave balance");
      }
    }

    LeaveBalanceDto balance = leaveService.getLeaveBalance(employeeId);

    ApiResponse<LeaveBalanceDto> response = ApiResponse.success(
        balance,
        HttpStatus.OK.value(),
        "Leave balance retrieved.",
        "Successfully retrieved leave balance for employee " + employeeId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/employees/{employeeId}/leave-requests")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> getLeaveRequestsForEmployee(
      @PathVariable(name = "employeeId") Integer employeeId,
      Authentication authentication) {
    // Allow manager or the employee themselves
    boolean isManager = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()));

    if (!isManager) {
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
      }

      try {
        EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(employeeId);
        String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
        if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
          throw new AccessDeniedException("Access denied: can only view your own leave requests");
        }
      } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
          | IllegalArgumentException ex) {
        throw new AccessDeniedException("Access denied: can only view your own leave requests");
      }
    }

    List<LeaveRequestDto> requests = leaveService.getRequestsForEmployee(employeeId);

    ApiResponse<List<LeaveRequestDto>> response = ApiResponse.success(
        requests,
        HttpStatus.OK.value(),
        "Leave requests retrieved.",
        "Retrieved " + requests.size() + " requests for employee " + employeeId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/leave-requests/{requestId}")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> getLeaveRequestById(
      @PathVariable(name = "requestId") Integer requestId,
      Authentication authentication) {
    // Allow manager or the employee themselves
    boolean isManager = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()));

    LeaveRequestDto request = leaveService.getRequestById(requestId);

    if (!isManager) {
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
      }

      try {
        EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(request.getEmployeeId());
        String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
        if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
          throw new AccessDeniedException("Access denied: can only view your own leave request");
        }
      } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
          | IllegalArgumentException ex) {
        throw new AccessDeniedException("Access denied: can only view your own leave request");
      }
    }

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        request,
        HttpStatus.OK.value(),
        "Leave request retrieved.",
        "Successfully retrieved LeaveRequest " + requestId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/api/leave-requests/{requestId}")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<Object>> deleteLeaveRequest(@PathVariable(name = "requestId") Integer requestId,
      Authentication authentication) {
    // Only the employee themselves may delete their request
    String jwtUserId = null;
    if (authentication instanceof JwtAuthenticationToken) {
      jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
    } else if (authentication.getPrincipal() instanceof Jwt) {
      jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
    }

    LeaveRequestDto request = leaveService.getRequestById(requestId);

    try {
      EmployeeDetailsDto emp = employeeService.getEmployeeDetailsById(request.getEmployeeId());
      String employeeUserId = emp.getUserId() == null ? null : emp.getUserId().toString();
      if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
        throw new AccessDeniedException("Access denied: can only delete your own leave request");
      }
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      throw new AccessDeniedException("Access denied: can only delete your own leave request");
    }

    leaveService.deleteLeaveRequest(requestId);

    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Leave request deleted successfully.",
        "LeaveRequest " + requestId + " was deleted.");
    return ResponseEntity.ok(response);
  }
}
