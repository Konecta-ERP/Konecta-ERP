package com.konecta.employeeservice.service;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konecta.employeeservice.dto.CreateEmployeeRequestDto;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import com.konecta.employeeservice.dto.UpdateEmployeeRequestDto;
import com.konecta.employeeservice.entity.Department;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.entity.User;
import com.konecta.employeeservice.repository.DepartmentRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.repository.UserRepository;
import com.konecta.employeeservice.service.specification.EmployeeSpecification;

@Service
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;

  @Autowired
  public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository,
      DepartmentRepository departmentRepository) {
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
    this.departmentRepository = departmentRepository;
  }

  public EmployeeDetailsDto getEmployeeDetailsById(Integer id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

    return convertToDto(employee);
  }

  @Transactional
  public EmployeeDetailsDto createEmployee(CreateEmployeeRequestDto dto) {
    // Check if email already exists
    if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
    }

    // Find the department
    Department department = departmentRepository.findById(dto.getDepartmentId())
        .orElseThrow(
            () -> new EntityNotFoundException("Department not found with id: " + dto.getDepartmentId()));

    // Create and save the User
    User newUser = new User();
    newUser.setEmail(dto.getEmail());
    newUser.setFirstName(dto.getFirstName());
    newUser.setLastName(dto.getLastName());
    newUser.setPhoneNumber(dto.getPhoneNumber());
    User savedUser = userRepository.save(newUser);

    // Create and save the Employee, linking the User and Department
    Employee newEmployee = new Employee();
    newEmployee.setUser(savedUser);
    newEmployee.setDepartment(department);
    newEmployee.setPositionTitle(dto.getPositionTitle());
    newEmployee.setHireDate(dto.getHireDate());
    newEmployee.setSalaryGross(dto.getSalaryGross());
    newEmployee.setSalaryNet(dto.getSalaryNet());
    Employee savedEmployee = employeeRepository.save(newEmployee);

    // Return the DTO
    return convertToDto(savedEmployee);
  }

  @Transactional
  public EmployeeDetailsDto updateEmployee(Integer id, UpdateEmployeeRequestDto dto) {
    // Find the existing employee
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

    // Update fields if they are present in the DTO
    if (dto.getPositionTitle() != null) {
      employee.setPositionTitle(dto.getPositionTitle());
    }
    if (dto.getSalaryGross() != null) {
      employee.setSalaryGross(dto.getSalaryGross());
    }
    if (dto.getSalaryNet() != null) {
      employee.setSalaryNet(dto.getSalaryNet());
    }

    // Update department if ID is provided
    if (dto.getDepartmentId() != null) {
      Department newDepartment = departmentRepository.findById(dto.getDepartmentId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Department not found with id: " + dto.getDepartmentId()));
      employee.setDepartment(newDepartment);
    }

    // Save the updated employee
    Employee updatedEmployee = employeeRepository.save(employee);

    // Return the updated DTO
    return convertToDto(updatedEmployee);
  }

  public List<EmployeeDetailsDto> searchEmployees(String name, String departmentName, String position) {
    // Create the dynamic specification
    Specification<Employee> spec = EmployeeSpecification.findByCriteria(name, departmentName, position);

    // Find all employees matching the spec
    List<Employee> employees = employeeRepository.findAll(spec);

    // Convert to DTOs and return
    return employees.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  private EmployeeDetailsDto convertToDto(Employee employee) {
    EmployeeDetailsDto dto = new EmployeeDetailsDto();

    // Map fields from Employee entity
    dto.setEmployeeId(employee.getId());
    dto.setPositionTitle(employee.getPositionTitle());
    dto.setHireDate(employee.getHireDate());
    dto.setSalaryGross(employee.getSalaryGross());
    dto.setSalaryNet(employee.getSalaryNet());

    // Map fields from the related User entity
    if (employee.getUser() != null) {
      dto.setUserId(employee.getUser().getId());
      dto.setEmail(employee.getUser().getEmail());
      dto.setFirstName(employee.getUser().getFirstName());
      dto.setLastName(employee.getUser().getLastName());
      dto.setPhoneNumber(employee.getUser().getPhoneNumber());
    }

    // Map fields from the related Department entity
    if (employee.getDepartment() != null) {
      dto.setDepartmentName(employee.getDepartment().getName());
    }

    return dto;
  }
}
