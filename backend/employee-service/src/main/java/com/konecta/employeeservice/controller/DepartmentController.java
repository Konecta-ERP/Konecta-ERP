package com.konecta.employeeservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.konecta.employeeservice.dto.CreateOrUpdateDepartmentDto;
import com.konecta.employeeservice.dto.DepartmentDto;
import com.konecta.employeeservice.dto.EmployeeLeavesDto;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;
import org.springframework.dao.EmptyResultDataAccessException;
import java.util.Objects;
import java.util.UUID;

import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.DepartmentService;
import com.konecta.employeeservice.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

  private final DepartmentService departmentService;
  private final EmployeeService employeeService;

  @Autowired
  public DepartmentController(DepartmentService departmentService,
      EmployeeService employeeService) {
    this.departmentService = departmentService;
    this.employeeService = employeeService;
  }

  @GetMapping("/{id}/leave-requests/next-month")
  @PreAuthorize("hasAuthority('HR_MANAGER') or hasAuthority('HR_ADMIN')")
  public ResponseEntity<ApiResponse<List<EmployeeLeavesDto>>> getDepartmentLeavesNextMonth(
      @PathVariable(name = "id") Integer id) {
    List<EmployeeLeavesDto> result = departmentService.getEmployeesLeavesForNextMonth(id);
    ApiResponse<List<EmployeeLeavesDto>> response = ApiResponse.success(
        result,
        HttpStatus.OK.value(),
        "Department leave requests retrieved.",
        "Retrieved " + result.size() + " employees with next-month leave requests for department " + id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/employees")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<List<EmployeeDetailsDto>>> getEmployeesInDepartment(
      @PathVariable(name = "id") Integer id,
      Authentication authentication) {
    // Ensure the caller is in the same department as requested
    String jwtUserId = null;
    if (authentication instanceof JwtAuthenticationToken) {
      jwtUserId = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("userId");
    } else if (authentication.getPrincipal() instanceof Jwt) {
      jwtUserId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
    }

    try {
      UUID uid = UUID.fromString(jwtUserId);
      Integer requesterDepartmentId = employeeService.getDepartmentIdForUser(uid);
      if (Objects.isNull(requesterDepartmentId) || !Objects.equals(requesterDepartmentId, id)) {
        throw new AccessDeniedException("Access denied: must be an employee in the requested department");
      }
    } catch (EntityNotFoundException | NoSuchElementException | EmptyResultDataAccessException
        | IllegalArgumentException ex) {
      throw new AccessDeniedException("Access denied: must be an employee in the requested department");
    }

    List<EmployeeDetailsDto> employees = departmentService.getEmployeesInDepartment(id);
    ApiResponse<List<EmployeeDetailsDto>> response = ApiResponse.success(
        employees,
        HttpStatus.OK.value(),
        "Employees retrieved.",
        "Retrieved " + employees.size() + " employees for department " + id);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('HR_ADMIN')")
  public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@RequestBody CreateOrUpdateDepartmentDto dto) {
    DepartmentDto newDepartment = departmentService.createDepartment(dto);
    ApiResponse<DepartmentDto> response = ApiResponse.success(
        newDepartment,
        HttpStatus.CREATED.value(),
        "Department created.",
        "Department created with id " + newDepartment.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
    List<DepartmentDto> departments = departmentService.getAllDepartments();
    ApiResponse<List<DepartmentDto>> response = ApiResponse.success(
        departments,
        HttpStatus.OK.value(),
        "Departments retrieved.",
        "Retrieved " + departments.size() + " departments");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('EMP')")
  public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentById(@PathVariable(name = "id") Integer id) {
    DepartmentDto department = departmentService.getDepartmentById(id);
    ApiResponse<DepartmentDto> response = ApiResponse.success(
        department,
        HttpStatus.OK.value(),
        "Department retrieved.",
        "Retrieved department with id " + id);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('HR_ADMIN') or hasAuthority('HR_MANAGER')")
  public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@PathVariable(name = "id") Integer id,
      @RequestBody CreateOrUpdateDepartmentDto dto) {
    DepartmentDto updatedDepartment = departmentService.updateDepartment(id, dto);
    ApiResponse<DepartmentDto> response = ApiResponse.success(
        updatedDepartment,
        HttpStatus.OK.value(),
        "Department updated.",
        "Updated department " + id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('HR_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> deleteDepartment(@PathVariable(name = "id") Integer id) {
    departmentService.deleteDepartment(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Department deleted.",
        "Department " + id + " deleted.");
    return ResponseEntity.ok(response);
  }
}
