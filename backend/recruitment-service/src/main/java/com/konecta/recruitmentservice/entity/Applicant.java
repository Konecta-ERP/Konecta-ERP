package com.konecta.recruitmentservice.entity;

import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "applicants")
@Getter
@Setter
@NoArgsConstructor
public class Applicant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "cv_filename")
  private String cvFileName;

  @Column(name = "cv_file_type")
  private String cvFileType;

  @Column(name = "cv_file_path")
  private String cvFilePath;

  @Column(name = "cover_letter", columnDefinition = "TEXT")
  private String coverLetter;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ApplicantStatus status = ApplicantStatus.APPLIED;

  @CreationTimestamp
  @Column(name = "applied_at", updatable = false)
  private LocalDateTime appliedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @ToString.Exclude
  private JobPost jobPost;

  @OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Offer offer;

  @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Set<Interview> interviews;

  @OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private CvScore cvScore;
}