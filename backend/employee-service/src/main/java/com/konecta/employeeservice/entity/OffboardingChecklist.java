package com.konecta.employeeservice.entity;

import com.konecta.employeeservice.model.OffboardingTask;
import com.konecta.employeeservice.model.enums.OffboardingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "offboarding_checklists")
@Data
public class OffboardingChecklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OffboardingStatus status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "tasks", columnDefinition = "jsonb")
  private List<OffboardingTask> tasks;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", unique = true, nullable = false)
  private Employee employee;
}