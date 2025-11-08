package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
  Optional<PayrollRecord> findByEmployeeIdAndYearMonth(Integer employeeId, String yearMonth);
}
