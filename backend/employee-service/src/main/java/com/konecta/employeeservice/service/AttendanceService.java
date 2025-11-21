package com.konecta.employeeservice.service;

import com.konecta.employeeservice.dto.AttendanceRecordDto;
import com.konecta.employeeservice.entity.AttendanceRecord;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.repository.AttendanceRecordRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AttendanceService {

  private final AttendanceRecordRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;

  @Autowired
  public AttendanceService(AttendanceRecordRepository attendanceRepository, EmployeeRepository employeeRepository) {
    this.attendanceRepository = attendanceRepository;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  public AttendanceRecordDto clockIn(Integer employeeId) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    LocalDate today = LocalDate.now();

    // Check if the user already has any clock-in for today
    if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, today)) {
      throw new IllegalStateException("Employee has already clocked in today.");
    }

    AttendanceRecord record = new AttendanceRecord();
    record.setEmployee(employee);
    record.setClockInTime(LocalDateTime.now());
    record.setDate(today);

    AttendanceRecord savedRecord = attendanceRepository.save(record);
    return convertToDto(savedRecord);
  }

  @Transactional
  public AttendanceRecordDto clockOut(Integer employeeId) {
    employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    LocalDate today = LocalDate.now();

    // Find the "open" attendance record from today
    AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDateAndClockOutTimeIsNull(employeeId, today)
        .orElseThrow(() -> new IllegalStateException("Cannot clock out. No open clock-in record found for today."));

    record.setClockOutTime(LocalDateTime.now());

    AttendanceRecord savedRecord = attendanceRepository.save(record);
    return convertToDto(savedRecord);
  }

  public AttendanceRecordDto getLatestAttendanceRecord(Integer employeeId) {
    employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

    java.util.Optional<AttendanceRecord> opt = attendanceRepository
        .findTopByEmployeeIdOrderByDateDescClockInTimeDesc(employeeId);

    return opt.map(this::convertToDto).orElse(null);
  }

  // --- DTO Converter ---
  private AttendanceRecordDto convertToDto(AttendanceRecord record) {
    AttendanceRecordDto dto = new AttendanceRecordDto();
    dto.setId(record.getId());
    dto.setEmployeeId(record.getEmployee().getId());
    dto.setClockInTime(record.getClockInTime());
    dto.setClockOutTime(record.getClockOutTime());
    dto.setDate(record.getDate());

    dto.setStatus(record.getClockOutTime() == null ? "CLOCKED_IN" : "CLOCKED_OUT");
    return dto;
  }
}
