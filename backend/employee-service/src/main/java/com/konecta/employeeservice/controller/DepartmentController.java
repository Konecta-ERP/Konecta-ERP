package com.konecta.employeeservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.konecta.employeeservice.dto.CreateOrUpdateDepartmentDto;
import com.konecta.employeeservice.dto.DepartmentDto;
import com.konecta.employeeservice.dto.response.ApiResponse;
import com.konecta.employeeservice.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

  private final DepartmentService departmentService;

  @Autowired
  public DepartmentController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  @PostMapping
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
  public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentById(@PathVariable Integer id) {
    DepartmentDto department = departmentService.getDepartmentById(id);
    ApiResponse<DepartmentDto> response = ApiResponse.success(
        department,
        HttpStatus.OK.value(),
        "Department retrieved.",
        "Retrieved department with id " + id);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@PathVariable Integer id,
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
  public ResponseEntity<ApiResponse<Object>> deleteDepartment(@PathVariable Integer id) {
    departmentService.deleteDepartment(id);
    ApiResponse<Object> response = ApiResponse.success(
        HttpStatus.NO_CONTENT.value(),
        "Department deleted.",
        "Department " + id + " deleted.");
    return ResponseEntity.ok(response);
  }
}
