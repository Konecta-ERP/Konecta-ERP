package com.konecta.recruitmentservice.entity;

import com.konecta.recruitmentservice.model.JobRequirement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "requirements", columnDefinition = "jsonb")
  private List<JobRequirement> requirements;

  @Column(name = "active", nullable = false)
  private boolean active = false; // Default to inactive until posted

  @CreationTimestamp
  @Column(name = "posted_at", updatable = false)
  private LocalDateTime postedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requisition_id", nullable = false)
  private JobRequisition jobRequisition;
}