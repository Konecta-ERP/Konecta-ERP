package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {

  /**
   * Finds an attendance record for a specific employee on a given date
   * where they have clocked in but not yet clocked out.
   */
  Optional<AttendanceRecord> findByEmployeeIdAndDateAndClockOutTimeIsNull(
      Integer employeeId,
      LocalDate date);

  /**
   * Finds any record for an employee on a given date,
   * used to prevent clocking in twice.
   */
  boolean existsByEmployeeIdAndDate(Integer employeeId, LocalDate date);
}
