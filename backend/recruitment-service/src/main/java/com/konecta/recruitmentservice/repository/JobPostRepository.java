package com.konecta.recruitmentservice.repository;

import com.konecta.recruitmentservice.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Integer>,
    JpaSpecificationExecutor<JobPost> {
}