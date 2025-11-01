package com.konecta.employeeservice.entity;

import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
public class LeaveRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING) // Store the ENUM as a String (e.g., "SICK")
  @Column(name = "request_type", nullable = false)
  private RequestType requestType;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RequestStatus status = RequestStatus.PENDING; // Default status

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;
}
