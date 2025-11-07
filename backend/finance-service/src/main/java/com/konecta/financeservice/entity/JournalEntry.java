package com.konecta.financeservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    /*
     * Many entries belong to one journal transaction
     * (the transaction groups debit/credit lines together).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private JournalTransaction transaction;

    /*
     * Each entry corresponds to a specific account in the chart of accounts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_pk", nullable = false)
    private Account account;

    @Column(name = "debit_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal creditAmount = BigDecimal.ZERO;
}
