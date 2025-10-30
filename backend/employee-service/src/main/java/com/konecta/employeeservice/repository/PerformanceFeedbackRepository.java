package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.PerformanceFeedback;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceFeedbackRepository extends JpaRepository<PerformanceFeedback, Integer> {
  List<PerformanceFeedback> findByRecipientId(Integer recipientId);
}
