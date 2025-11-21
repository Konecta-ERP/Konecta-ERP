package com.konecta.employeeservice.service;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

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
  private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

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

  public Integer getDepartmentIdForUser(UUID userId) {
    Employee employee = employeeRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with userId: " + userId));
    return employee.getDepartment() == null ? null : employee.getDepartment().getId();
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

    // Prepare request entity
    HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(createUserPayload, headers);
    java.util.UUID createdUserId;
    try {
      // Log the outgoing payload at DEBUG, masking the password
      if (logger.isDebugEnabled()) {
        var masked = new java.util.HashMap<>(createUserPayload);
        if (masked.containsKey("password")) {
          masked.put("password", "[MASKED]");
        }
        logger.debug("POST {} -> payload: {}", identityUrl, masked);
      }

      ResponseEntity<java.util.Map<String, Object>> resp = restTemplate.exchange(identityUrl, HttpMethod.POST,
          requestEntity, new ParameterizedTypeReference<java.util.Map<String, Object>>() {
          });

      if (logger.isDebugEnabled()) {
        logger.debug("Identity service responded: status={} body={}",
            resp == null ? "null" : resp.getStatusCode().value(),
            resp == null ? null : resp.getBody());
      }

      if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
        String status = resp == null ? "null response" : String.valueOf(resp.getStatusCode().value());
        throw new IllegalStateException("Identity service returned non-2xx status: " + status);
      }

      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> identityResponse = (java.util.Map<String, Object>) resp.getBody();

      if (identityResponse == null) {
        throw new IllegalStateException("Identity service returned empty body (raw response: " + resp + ")");
      }

      Object dataObj = identityResponse.get("data");
      if (!(dataObj instanceof java.util.Map)) {
        // Try some common alternative shapes and include the raw response for debugging
        String raw = identityResponse.toString();
        throw new IllegalStateException(
            "Identity response 'data' is missing or not an object. rawResponse=" + raw);
      }

      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> data = (java.util.Map<String, Object>) dataObj;
      Object idObj = data.get("id");
      if (idObj == null) {
        throw new IllegalStateException("Identity service did not return created user id; response data: " + data);
      }
      createdUserId = java.util.UUID.fromString(idObj.toString());
    } catch (HttpClientErrorException e) {
      throw new IllegalStateException(
          "Identity service error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
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
    // Instead, fetch candidates by department/position, convert to DTOs (which
    // enrich via Identity service) and filter by name in-memory.
    Specification<Employee> spec = EmployeeSpecification.findByCriteria(null, departmentName, position);

    List<Employee> candidates = employeeRepository.findAll(spec);

    return candidates.stream()
        .map(this::convertToDto)
        .filter(dto -> {
          if (name == null || name.isBlank())
            return true;
          String lower = name.toLowerCase();
          String fn = dto.getFirstName() == null ? "" : dto.getFirstName().toLowerCase();
          String ln = dto.getLastName() == null ? "" : dto.getLastName().toLowerCase();
          return fn.contains(lower) || ln.contains(lower);
        })
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

    // Enrich DTO with data from identity service (email, name, phone)
    try {
      addUserInfo(dto);
    } catch (Exception e) {
      // Don't fail the whole operation if identity is unavailable; log at debug
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to enrich EmployeeDetailsDto with identity info for userId={}: {}",
            dto.getUserId(), e.getMessage());
      }
    }
    // Map fields from the related Department entity
    if (employee.getDepartment() != null) {
      dto.setDepartmentName(employee.getDepartment().getName());
    }

    return dto;
  }

  private void addUserInfo(EmployeeDetailsDto dto) {
    if (dto == null || dto.getUserId() == null) {
      return;
    }

    String identityUrl = "http://identity-service/api/identity/users/" + dto.getUserId();

    // Forward bearer token if present
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String bearerToken = null;
    if (auth instanceof JwtAuthenticationToken) {
      bearerToken = ((JwtAuthenticationToken) auth).getToken().getTokenValue();
    } else if (auth != null && auth.getCredentials() instanceof String) {
      bearerToken = (String) auth.getCredentials();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    if (bearerToken != null && !bearerToken.isBlank()) {
      headers.setBearerAuth(bearerToken);
    }

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    java.util.Map<String, Object> body = null;
    try {
      ResponseEntity<java.util.Map<String, Object>> resp = restTemplate.exchange(identityUrl, HttpMethod.GET,
          entity, new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {
          });

      if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Identity service returned non-2xx for userId={} status={}", dto.getUserId(),
              resp == null ? "null" : resp.getStatusCode().value());
        }
        return;
      }

      body = resp.getBody();
    } catch (org.springframework.web.client.HttpClientErrorException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Identity service returned error for userId={}: {} {}", dto.getUserId(), e.getStatusCode(),
            e.getResponseBodyAsString());
      }
      return;
    } catch (org.springframework.web.client.RestClientException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed calling identity service for userId={}: {}", dto.getUserId(), e.getMessage());
      }
      return;
    }

    if (body == null) {
      return;
    }

    Object dataObj = body.get("data");
    if (!(dataObj instanceof java.util.Map)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Unexpected identity response shape for userId={}: {}", dto.getUserId(), body);
      }
      return;
    }

    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> data = (java.util.Map<String, Object>) dataObj;

    // Map known fields
    Object email = data.get("email");
    if (email != null)
      dto.setEmail(email.toString());
    Object firstName = data.get("firstName");
    if (firstName != null)
      dto.setFirstName(firstName.toString());
    Object lastName = data.get("lastName");
    if (lastName != null)
      dto.setLastName(lastName.toString());
    Object phone = data.get("phone");
    if (phone != null)
      dto.setPhoneNumber(phone.toString());
  }
}
