package com.konecta.recruitmentservice.repository;

import com.konecta.recruitmentservice.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {
  List<Applicant> findByJobPostId(Integer postId);
}