package com.konecta.recruitmentservice.service.specification;

import com.konecta.recruitmentservice.entity.JobRequisition;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class RequisitionSpecification {

  public static Specification<JobRequisition> findByCriteria(Integer departmentId, RequisitionStatus status) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (departmentId != null) {
        predicates.add(cb.equal(root.get("departmentId"), departmentId));
      }
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}