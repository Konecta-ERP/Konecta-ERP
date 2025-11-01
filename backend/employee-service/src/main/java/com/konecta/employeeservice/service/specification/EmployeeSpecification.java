package com.konecta.employeeservice.service.specification;

import org.springframework.data.jpa.domain.Specification;

import com.konecta.employeeservice.entity.Employee;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

  public static Specification<Employee> findByCriteria(String name, String departmentName, String position) {
    return (root, query, criteriaBuilder) -> {

      List<Predicate> predicates = new ArrayList<>();

      // 1. Search by Name (in User table)
      if (name != null && !name.isEmpty()) {
        String nameLike = "%" + name.toLowerCase() + "%";
        // This joins Employee -> User and searches first_name OR last_name
        predicates.add(criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("firstName")), nameLike),
            criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("lastName")), nameLike)));
      }

      // 2. Search by Department Name (in Department table)
      if (departmentName != null && !departmentName.isEmpty()) {
        // This joins Employee -> Department and searches name
        predicates.add(criteriaBuilder.equal(
            root.join("department").get("name"), departmentName));
      }

      // 3. Search by Position (in Employee table)
      if (position != null && !position.isEmpty()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("positionTitle")), "%" + position.toLowerCase() + "%"));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
