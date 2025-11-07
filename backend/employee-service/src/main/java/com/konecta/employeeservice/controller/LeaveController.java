package com.konecta.employeeservice.controller;

import com.konecta.employeeservice.dto.CreateLeaveRequestDto;
import com.konecta.employeeservice.dto.LeaveBalanceDto;
import com.konecta.employeeservice.dto.LeaveRequestDto;
import com.konecta.employeeservice.dto.UpdateLeaveStatusDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LeaveController {

  private final LeaveService leaveService;

  @Autowired
  public LeaveController(LeaveService leaveService) {
    this.leaveService = leaveService;
  }

  @PostMapping("/employees/{employeeId}/leave-requests")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> submitLeaveRequest(
      @PathVariable Integer employeeId,
      @RequestBody CreateLeaveRequestDto dto) {
    LeaveRequestDto newRequest = leaveService.createLeaveRequest(employeeId, dto);

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        newRequest,
        HttpStatus.CREATED.value(),
        "Leave request submitted successfully.",
        "LeaveRequest created with id " + newRequest.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PatchMapping("/leave-requests/{requestId}/status")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> updateLeaveRequestStatus(
      @PathVariable Integer requestId,
      @RequestBody UpdateLeaveStatusDto dto) {
    LeaveRequestDto updatedRequest = leaveService.updateRequestStatus(requestId, dto);

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        updatedRequest,
        HttpStatus.OK.value(),
        "Request status updated.",
        "LeaveRequest " + requestId + " status updated to " + dto.getStatus());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/employees/{employeeId}/leave-balance")
  public ResponseEntity<ApiResponse<LeaveBalanceDto>> getEmployeeLeaveBalance(@PathVariable Integer employeeId) {
    LeaveBalanceDto balance = leaveService.getLeaveBalance(employeeId);

    ApiResponse<LeaveBalanceDto> response = ApiResponse.success(
        balance,
        HttpStatus.OK.value(),
        "Leave balance retrieved.",
        "Successfully retrieved leave balance for employee " + employeeId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/employees/{employeeId}/leave-requests")
  public ResponseEntity<ApiResponse<List<LeaveRequestDto>>> getLeaveRequestsForEmployee(
      @PathVariable Integer employeeId) {
    List<LeaveRequestDto> requests = leaveService.getRequestsForEmployee(employeeId);

    ApiResponse<List<LeaveRequestDto>> response = ApiResponse.success(
        requests,
        HttpStatus.OK.value(),
        "Leave requests retrieved.",
        "Retrieved " + requests.size() + " requests for employee " + employeeId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/leave-requests/{requestId}")
  public ResponseEntity<ApiResponse<LeaveRequestDto>> getLeaveRequestById(@PathVariable Integer requestId) {
    LeaveRequestDto request = leaveService.getRequestById(requestId);

    ApiResponse<LeaveRequestDto> response = ApiResponse.success(
        request,
        HttpStatus.OK.value(),
        "Leave request retrieved.",
        "Successfully retrieved LeaveRequest " + requestId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/leave-requests/{requestId}")
  public ResponseEntity<ApiResponse<Object>> deleteLeaveRequest(@PathVariable Integer requestId) {
    leaveService.deleteLeaveRequest(requestId);

    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Leave request deleted successfully.",
        "LeaveRequest " + requestId + " was deleted.");
    return ResponseEntity.ok(response);
  }
}
