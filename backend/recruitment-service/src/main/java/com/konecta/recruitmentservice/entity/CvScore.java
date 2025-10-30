package com.konecta.recruitmentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cv_scores")
@Getter
@Setter
@NoArgsConstructor
public class CvScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "score")
  private Integer score;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  @ToString.Exclude
  private Applicant applicant;
}