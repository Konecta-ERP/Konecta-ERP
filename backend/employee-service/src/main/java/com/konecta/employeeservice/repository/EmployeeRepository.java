package com.konecta.employeeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.konecta.employeeservice.entity.Employee;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {

  List<Employee> findByDepartmentId(Integer departmentId);

  List<Employee> findByPositionTitle(String positionTitle);
}
