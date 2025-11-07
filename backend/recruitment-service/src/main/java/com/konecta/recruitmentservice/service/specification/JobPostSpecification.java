package com.konecta.recruitmentservice.service.specification;

import com.konecta.recruitmentservice.entity.JobPost;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class JobPostSpecification {

  public static Specification<JobPost> findByCriteria(String position, Integer departmentId, Boolean active) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by position title
      if (position != null && !position.isEmpty()) {
        predicates.add(cb.like(cb.lower(root.get("title")), "%" + position.toLowerCase() + "%"));
      }

      // Filter by department
      if (departmentId != null) {
        // Joins JobPost -> JobRequisition and checks departmentId
        predicates.add(cb.equal(root.join("jobRequisition", JoinType.INNER).get("departmentId"), departmentId));
      }

      // Filter by active status
      if (active != null) {
        predicates.add(cb.equal(root.get("active"), active));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}