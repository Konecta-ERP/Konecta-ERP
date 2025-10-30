package com.konecta.recruitmentservice.repository;

import com.konecta.recruitmentservice.entity.JobRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRequisitionRepository extends JpaRepository<JobRequisition, Integer>,
    JpaSpecificationExecutor<JobRequisition> {
}