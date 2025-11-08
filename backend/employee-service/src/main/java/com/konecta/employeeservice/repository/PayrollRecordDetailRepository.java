package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.PayrollRecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRecordDetailRepository extends JpaRepository<PayrollRecordDetail, Long> {
}
