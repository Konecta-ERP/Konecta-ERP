package com.konecta.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "payroll_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Avoid including `details` in generated toString/equals to prevent recursion
@ToString(exclude = "details")
@EqualsAndHashCode(exclude = "details")
public class PayrollRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_id", nullable = false)
  private Integer employeeId;

  @Column(name = "year_month", nullable = false)
  private String yearMonth; // format YYYY-MM

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd;

  @Column(name = "basic_pay", nullable = false)
  private BigDecimal basicPay;

  @Column(name = "overtime_pay", nullable = false)
  private BigDecimal overtimePay;

  @Column(name = "deductions", nullable = false)
  private BigDecimal deductions;

  @Column(name = "net_pay", nullable = false)
  private BigDecimal netPay;

  @OneToMany(mappedBy = "payrollRecord", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PayrollRecordDetail> details;
}
