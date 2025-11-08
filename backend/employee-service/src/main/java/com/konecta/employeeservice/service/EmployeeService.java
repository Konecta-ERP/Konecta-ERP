package com.konecta.employeeservice.service;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.konecta.employeeservice.dto.CreateEmployeeRequestDto;
import com.konecta.employeeservice.dto.EmployeeDetailsDto;
import com.konecta.employeeservice.dto.UpdateEmployeeRequestDto;
import com.konecta.employeeservice.entity.Department;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.repository.DepartmentRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.service.specification.EmployeeSpecification;

@Service
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final org.springframework.web.client.RestTemplate restTemplate;
  private final DepartmentRepository departmentRepository;

  @Autowired
  public EmployeeService(EmployeeRepository employeeRepository,
      DepartmentRepository departmentRepository,
      org.springframework.web.client.RestTemplate restTemplate) {
    this.employeeRepository = employeeRepository;
    this.departmentRepository = departmentRepository;
    this.restTemplate = restTemplate;
  }

  public EmployeeDetailsDto getEmployeeDetailsById(Integer id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

    return convertToDto(employee);
  }

  public EmployeeDetailsDto getEmployeeDetailsByUserId(java.util.UUID userId) {
    Employee employee = employeeRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with userId: " + userId));
    return convertToDto(employee);
  }

  @Transactional
  public EmployeeDetailsDto createEmployee(CreateEmployeeRequestDto dto) {
    // Delegate user creation to Identity service
    // Password is now mandatory; throw exception if missing or blank
    if (dto.getPassword() == null || dto.getPassword().isBlank()) {
        throw new IllegalArgumentException("Password is required when creating an employee.");
    }
    String passwordToUse = dto.getPassword();
    String roleToUse = dto.getRole() != null && !dto.getRole().isBlank() ? dto.getRole() : "ASSOCIATE";

    var createUserPayload = new java.util.HashMap<String, Object>();
    createUserPayload.put("firstName", dto.getFirstName());
    createUserPayload.put("lastName", dto.getLastName());
    createUserPayload.put("email", dto.getEmail());
    createUserPayload.put("phone", dto.getPhoneNumber());
    createUserPayload.put("password", passwordToUse);
    createUserPayload.put("role", roleToUse);

    // Identity returns ApiResponse<UserResponse>. We'll POST and extract data.id
    String identityUrl = "http://identity-service/api/identity/users";

    // Extract bearer token from security context (if present) and forward to
    // identity service
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String bearerToken = null;
    if (auth instanceof JwtAuthenticationToken) {
      bearerToken = ((JwtAuthenticationToken) auth).getToken().getTokenValue();
    } else if (auth != null && auth.getCredentials() instanceof String) {
      bearerToken = (String) auth.getCredentials();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    if (bearerToken != null && !bearerToken.isBlank()) {
      headers.setBearerAuth(bearerToken);
    }

    HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(createUserPayload, headers);
    java.util.UUID createdUserId;
    try {
      ResponseEntity<java.util.Map> resp = restTemplate.exchange(identityUrl, HttpMethod.POST, requestEntity,
          java.util.Map.class);

      if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
        String status = resp == null ? "null response" : String.valueOf(resp.getStatusCodeValue());
        throw new IllegalStateException("Identity service returned non-2xx status: " + status);
      }

      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> identityResponse = (java.util.Map<String, Object>) resp.getBody();

      if (identityResponse == null) {
        throw new IllegalStateException("Identity service returned empty body");
      }

      Object dataObj = identityResponse.get("data");
      if (!(dataObj instanceof java.util.Map)) {
        throw new IllegalStateException("Identity response 'data' is missing or not an object: " + dataObj);
      }

      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> data = (java.util.Map<String, Object>) dataObj;
      Object idObj = data.get("id");
      if (idObj == null) {
        throw new IllegalStateException("Identity service did not return created user id; response data: " + data);
      }
      createdUserId = java.util.UUID.fromString(idObj.toString());
    } catch (org.springframework.web.client.HttpClientErrorException e) {
      throw new IllegalStateException(
          "Identity service error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
    } catch (org.springframework.web.client.RestClientException e) {
      throw new IllegalStateException("Failed to call identity service: " + e.getMessage(), e);
    }

    // Find the department
    Department department = departmentRepository.findById(dto.getDepartmentId())
        .orElseThrow(
            () -> new EntityNotFoundException("Department not found with id: " + dto.getDepartmentId()));

    // Create and save the Employee, linking the created userId and Department
    Employee newEmployee = new Employee();
    newEmployee.setUserId(createdUserId);
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

    // Store the userId reference (Identity service is source of truth for user
    // details)
    if (employee.getUserId() != null) {
      dto.setUserId(employee.getUserId());
    }

    // Map fields from the related Department entity
    if (employee.getDepartment() != null) {
      dto.setDepartmentName(employee.getDepartment().getName());
    }

    return dto;
  }
}
