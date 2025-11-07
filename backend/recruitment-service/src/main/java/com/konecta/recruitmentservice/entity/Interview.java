package com.konecta.recruitmentservice.entity;

import com.konecta.recruitmentservice.model.enums.InterviewMode;
import com.konecta.recruitmentservice.model.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
public class Interview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "feedback", columnDefinition = "TEXT")
  private String feedback;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private InterviewStatus status = InterviewStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "mode", nullable = false)
  private InterviewMode mode;

  @Column(name = "scheduled_at", nullable = false)
  private LocalDateTime scheduledAt;

  // This is the ID of an Employee from the employee-service
  @Column(name = "interviewer_id")
  private Integer interviewerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  @ToString.Exclude
  private Applicant applicant;
}