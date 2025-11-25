package com.konecta.financeservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.konecta.financeservice.model.enums.PeriodStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_label", nullable = false, unique = true, length = 50)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", length = 20)
    private PeriodStatus status = PeriodStatus.OPEN;

    @Column(name = "created_by_user_id", nullable = false)
    private String createdByUserId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "time_to_close")
    private Long timeToClose;

    @Column(name = "revenue_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal revenueBudget = BigDecimal.ZERO;

    @Column(name = "cogs_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal cogsBudget = BigDecimal.ZERO;

    @Column(name = "opex_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal opexBudget = BigDecimal.ZERO;

    @Column(name = "other_income_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal otherIncomeBudget = BigDecimal.ZERO;

    @Column(name = "other_expense_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal otherExpenseBudget = BigDecimal.ZERO;
}
