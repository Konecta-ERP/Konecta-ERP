package com.konecta.employeeservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Objects;
import org.springframework.web.bind.annotation.*;

import com.konecta.employeeservice.dto.CreateEmployeeRequestDto;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import com.konecta.employeeservice.dto.UpdateEmployeeRequestDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.EmployeeService;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  @Autowired
  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> getEmployeeById(@PathVariable(name = "id") Integer id,
      Authentication authentication) {
    // Allow managers and admins to access any employee
    boolean isManagerOrAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> "MANAGER".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

    EmployeeDetailsDto employeeDetails = employeeService.getEmployeeDetailsById(id);

    if (!isManagerOrAdmin) {
      // For non-manager/admin users, ensure the JWT contains a userId claim that matches the employee's userId
      String jwtUserId = null;
      if (authentication instanceof JwtAuthenticationToken) {
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        jwtUserId = jwt.getClaimAsString("userId");
      } else if (authentication.getPrincipal() instanceof Jwt) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        jwtUserId = jwt.getClaimAsString("userId");
      }

      String employeeUserId = employeeDetails.getUserId() == null ? null : employeeDetails.getUserId().toString();

      if (Objects.isNull(jwtUserId) || !Objects.equals(jwtUserId, employeeUserId)) {
        throw new AccessDeniedException("Access denied: you are not allowed to view this employee");
      }
    }

    ApiResponse<EmployeeDetailsDto> response = ApiResponse.success(
        employeeDetails,
        HttpStatus.OK.value(),
        "Employee details retrieved.",
        "Retrieved employee with id " + id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/by-user/{userId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> getEmployeeByUserId(@PathVariable("userId") java.util.UUID userId,
      Authentication authentication) {
    // Only the user themself may call this endpoint
    String jwtUserId = null;
    if (authentication instanceof JwtAuthenticationToken) {
      jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
    } else if (authentication.getPrincipal() instanceof Jwt) {
      jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
    }

    if (jwtUserId == null || !jwtUserId.equals(userId.toString())) {
      throw new AccessDeniedException("Access denied: can only fetch your own employee record");
    }

    EmployeeDetailsDto employeeDetails = employeeService.getEmployeeDetailsByUserId(userId);
    ApiResponse<EmployeeDetailsDto> response = ApiResponse.success(
        employeeDetails,
        HttpStatus.OK.value(),
        "Employee details retrieved.",
        "Retrieved employee for userId " + userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> createEmployee(
      @RequestBody CreateEmployeeRequestDto createDto) {
    EmployeeDetailsDto newEmployee = employeeService.createEmployee(createDto);
    ApiResponse<EmployeeDetailsDto> response = ApiResponse.success(
        newEmployee,
        HttpStatus.CREATED.value(),
        "Employee created.",
        "Employee created with id " + newEmployee.getEmployeeId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  // GET /employees/search?name=John&department=Engineering&position=Manager
  @GetMapping("/search")
  @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<List<EmployeeDetailsDto>>> searchEmployees(
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "department", required = false) String department,
      @RequestParam(name = "position", required = false) String position) {
    List<EmployeeDetailsDto> employees = employeeService.searchEmployees(name, department, position);
    ApiResponse<List<EmployeeDetailsDto>> response = ApiResponse.success(
        employees,
        HttpStatus.OK.value(),
        "Employees list retrieved.",
        "Search returned " + employees.size() + " employees");
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> updateEmployee(
      @PathVariable(name = "id") Integer id,
      @RequestBody UpdateEmployeeRequestDto updateDto) {
    EmployeeDetailsDto updatedEmployee = employeeService.updateEmployee(id, updateDto);
    ApiResponse<EmployeeDetailsDto> response = ApiResponse.success(
        updatedEmployee,
        HttpStatus.OK.value(),
        "Employee updated.",
        "Updated employee " + id);
    return ResponseEntity.ok(response);
  }
}
