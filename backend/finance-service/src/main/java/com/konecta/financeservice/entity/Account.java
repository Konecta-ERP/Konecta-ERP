package com.konecta.financeservice.entity;

import com.konecta.financeservice.model.enums.AccountStatus;
import com.konecta.financeservice.model.enums.AccountType;
import com.konecta.financeservice.model.enums.CashSource;
import com.konecta.financeservice.model.enums.ProfitLossMapping;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_pk")
    private Long accountPK;

    @Column(name = "account_id", length = 20, unique = true, nullable = false)
    private String accountId;

    @Column(name = "account_name", nullable = false, unique = true, length = 255)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pl_mapping", nullable = false, length = 20)
    private ProfitLossMapping plMapping = ProfitLossMapping.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "cash_source", nullable = false, length = 20)
    private CashSource cashSource = CashSource.NONE;

    @Column(name = "is_cash_account", nullable = false)
    private boolean isCashAccount = false;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "has_transactions", nullable = false)
    private boolean hasTransactions = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
