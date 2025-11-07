package com.konecta.employeeservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_goals")
@Data
public class EmployeeGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "target")
    private String target;

    @Column(name = "cycle")
    private String cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
