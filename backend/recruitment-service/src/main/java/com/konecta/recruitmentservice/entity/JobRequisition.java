package com.konecta.recruitmentservice.entity;

import com.konecta.recruitmentservice.model.enums.RequisitionPriority;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "job_requisitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRequisition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "reason")
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private RequisitionPriority priority;

  @Column(name = "openings", nullable = false)
  private Integer openings;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RequisitionStatus status = RequisitionStatus.PENDING;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "department_id", nullable = false)
  private Integer departmentId;

  @OneToMany(mappedBy = "jobRequisition", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<JobPost> jobPosts;
}