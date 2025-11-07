package com.konecta.employeeservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> getEmployeeById(@PathVariable Integer id) {
    EmployeeDetailsDto employeeDetails = employeeService.getEmployeeDetailsById(id);

    ApiResponse<EmployeeDetailsDto> response = ApiResponse.success(
        employeeDetails,
        HttpStatus.OK.value(),
        "Employee details retrieved.",
        "Retrieved employee with id " + id);
    return ResponseEntity.ok(response);
  }

  @PostMapping
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
  public ResponseEntity<ApiResponse<EmployeeDetailsDto>> updateEmployee(
      @PathVariable Integer id,
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
