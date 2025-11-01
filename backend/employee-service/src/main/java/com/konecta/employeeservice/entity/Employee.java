package com.konecta.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true)
  private User user;

  @Column(name = "position_title")
  private String positionTitle;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(name = "salary_gross")
  private BigDecimal salaryGross;

  @Column(name = "salary_net")
  private BigDecimal salaryNet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<EmployeeGoal> goals;

  @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PerformanceFeedback> receivedFeedbacks;

  @OneToMany(mappedBy = "giver", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PerformanceFeedback> givenFeedbacks;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<LeaveRequest> leaveRequests;
}
