package com.konecta.employeeservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konecta.employeeservice.dto.CreateOrUpdateDepartmentDto;
import com.konecta.employeeservice.dto.DepartmentDto;
import com.konecta.employeeservice.entity.Department;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.repository.DepartmentRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.dto.EmployeeLeavesDto;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import com.konecta.employeeservice.dto.LeaveRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final LeaveService leaveService;
  private final EmployeeService employeeService;

  @Autowired
  public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
      LeaveService leaveService, EmployeeService employeeService) {
    this.departmentRepository = departmentRepository;
    this.employeeRepository = employeeRepository;
    this.leaveService = leaveService;
    this.employeeService = employeeService;
  }

  public List<DepartmentDto> getAllDepartments() {
    return departmentRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public DepartmentDto getDepartmentById(Integer id) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
    return convertToDto(department);
  }

  @Transactional
  public DepartmentDto createDepartment(CreateOrUpdateDepartmentDto dto) {
    Department department = new Department();
    department.setName(dto.getName());

    if (dto.getManagerId() != null) {
      Employee manager = employeeRepository.findById(dto.getManagerId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Manager (Employee) not found with id: " + dto.getManagerId()));
      department.setManager(manager);
    }

    Department savedDepartment = departmentRepository.save(department);
    return convertToDto(savedDepartment);
  }

  @Transactional
  public DepartmentDto updateDepartment(Integer id, CreateOrUpdateDepartmentDto dto) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

    department.setName(dto.getName());

    if (dto.getManagerId() != null) {
      Employee manager = employeeRepository.findById(dto.getManagerId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Manager (Employee) not found with id: " + dto.getManagerId()));
      department.setManager(manager);
    } else {
      department.setManager(null); // Allow setting manager to null
    }

    Department updatedDepartment = departmentRepository.save(department);
    return convertToDto(updatedDepartment);
  }

  @Transactional
  public void deleteDepartment(Integer id) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

    // Basic check to prevent deleting a department with employees
    if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete department. It has " + department.getEmployees().size() + " employees.");
    }

    departmentRepository.delete(department);
  }

  private DepartmentDto convertToDto(Department department) {
    DepartmentDto dto = new DepartmentDto();
    dto.setId(department.getId());
    dto.setName(department.getName());
    if (department.getManager() != null) {
      dto.setManagerId(department.getManager().getId());
      // Manager user details are in Identity service; optionally enrich via identity client later
      dto.setManagerName(null);
    }
    return dto;
  }

  public java.util.List<EmployeeLeavesDto> getEmployeesLeavesForNextMonth(Integer departmentId) {
    if (!departmentRepository.existsById(departmentId)) {
      throw new EntityNotFoundException("Department not found with id: " + departmentId);
    }

    java.time.YearMonth nextMonth = java.time.YearMonth.now().plusMonths(1);
    java.time.LocalDate start = nextMonth.atDay(1);
    java.time.LocalDate end = nextMonth.atEndOfMonth();

    java.util.List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);

    java.util.List<EmployeeLeavesDto> result = new java.util.ArrayList<>();
    for (Employee e : employees) {
      EmployeeDetailsDto empDto = employeeService.getEmployeeDetailsById(e.getId());
      java.util.List<LeaveRequestDto> leaves = leaveService.getRequestsForEmployeeInRange(e.getId(), start, end);
      EmployeeLeavesDto el = new EmployeeLeavesDto();
      el.setEmployee(empDto);
      el.setLeaveRequests(leaves);
      result.add(el);
    }

    return result;
  }
}
