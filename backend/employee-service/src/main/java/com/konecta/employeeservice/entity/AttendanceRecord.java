package com.konecta.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Data
public class AttendanceRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(name = "clock_in_time", nullable = false, updatable = false)
  private LocalDateTime clockInTime;

  @Column(name = "clock_out_time")
  private LocalDateTime clockOutTime;

  @Column(name = "date", nullable = false, updatable = false)
  private LocalDate date;
}
