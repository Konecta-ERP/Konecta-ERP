package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.EmployeeGoal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoal, Integer> {
  List<EmployeeGoal> findByEmployeeId(Integer employeeId);
}
