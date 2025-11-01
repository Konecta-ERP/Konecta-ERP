package com.konecta.employeeservice.service;

import com.konecta.employeeservice.dto.InitiateOffboardingDto;
import com.konecta.employeeservice.dto.OffboardingChecklistDto;
import com.konecta.employeeservice.dto.UpdateChecklistDto;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.entity.OffboardingChecklist;
import com.konecta.employeeservice.model.enums.OffboardingStatus;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.repository.OffboardingChecklistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OffboardingService {

  private final OffboardingChecklistRepository checklistRepository;
  private final EmployeeRepository employeeRepository;

  @Autowired
  public OffboardingService(OffboardingChecklistRepository checklistRepository, EmployeeRepository employeeRepository) {
    this.checklistRepository = checklistRepository;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public OffboardingChecklistDto initiateOffboarding(Integer employeeId, InitiateOffboardingDto dto) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    if (checklistRepository.existsByEmployeeId(employeeId)) {
      throw new IllegalStateException("An offboarding checklist already exists for this employee.");
    }

    OffboardingChecklist checklist = new OffboardingChecklist();
    checklist.setEmployee(employee);
    checklist.setTasks(dto.getTasks());
    checklist.setStatus(OffboardingStatus.IN_PROGRESS);

    OffboardingChecklist saved = checklistRepository.save(checklist);
    return convertToDto(saved);
  }

  @Transactional(readOnly = true)
  public OffboardingChecklistDto getChecklistForEmployee(Integer employeeId) {
    OffboardingChecklist checklist = checklistRepository.findByEmployeeId(employeeId)
        .orElseThrow(
            () -> new EntityNotFoundException("No offboarding checklist found for employee id: " + employeeId));
    return convertToDto(checklist);
  }

  @Transactional
  public OffboardingChecklistDto updateChecklist(Integer checklistId, UpdateChecklistDto dto) {
    OffboardingChecklist checklist = checklistRepository.findById(checklistId)
        .orElseThrow(() -> new EntityNotFoundException("Checklist not found with id: " + checklistId));

    checklist.setTasks(dto.getTasks());

    // Auto-update status if provided, otherwise check if all tasks are done
    if (dto.getStatus() != null) {
      checklist.setStatus(dto.getStatus());
    } else {
      boolean allComplete = dto.getTasks().stream().allMatch(task -> task.isComplete());
      checklist.setStatus(allComplete ? OffboardingStatus.DONE : OffboardingStatus.IN_PROGRESS);
    }

    OffboardingChecklist updated = checklistRepository.save(checklist);
    return convertToDto(updated);
  }

  private OffboardingChecklistDto convertToDto(OffboardingChecklist checklist) {
    OffboardingChecklistDto dto = new OffboardingChecklistDto();
    dto.setId(checklist.getId());
    dto.setEmployeeId(checklist.getEmployee().getId());
    dto.setStatus(checklist.getStatus());
    dto.setTasks(checklist.getTasks());
    return dto;
  }
}