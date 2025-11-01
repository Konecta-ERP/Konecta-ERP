package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.OffboardingChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OffboardingChecklistRepository extends JpaRepository<OffboardingChecklist, Integer> {

  Optional<OffboardingChecklist> findByEmployeeId(Integer employeeId);

  boolean existsByEmployeeId(Integer employeeId);
}