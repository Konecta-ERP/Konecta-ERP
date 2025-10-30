package com.konecta.recruitmentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
public class Offer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "net_salary")
  private BigDecimal netSalary;

  @Column(name = "gross_salary")
  private BigDecimal grossSalary;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "benefits", columnDefinition = "jsonb")
  private Map<String, Object> benefits;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false, unique = true)
  @ToString.Exclude
  private Applicant applicant;
}