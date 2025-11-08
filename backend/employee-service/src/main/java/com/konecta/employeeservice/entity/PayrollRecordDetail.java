package com.konecta.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_record_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Exclude back-reference from toString/equals to prevent StackOverflow via
// bidirectional link
@ToString(exclude = "payrollRecord")
@EqualsAndHashCode(exclude = "payrollRecord")
public class PayrollRecordDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payroll_record_id", nullable = false)
  private PayrollRecord payrollRecord;

  @Column(name = "type", nullable = false)
  private String type; // e.g., BASIC, OVERTIME, DEDUCTION

  @Column(name = "description")
  private String description;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;
}
