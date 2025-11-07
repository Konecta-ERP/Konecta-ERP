package com.konecta.financeservice.repository;

import com.konecta.financeservice.dto.TrialBalanceRowDTO;
import com.konecta.financeservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Account, Long> {
    @Query("""
                SELECT new com.konecta.financeservice.dto.TrialBalanceRowDTO(
                    a.accountId,
                    a.accountName,
                    a.accountType,
                    SUM(je.debitAmount),
                    SUM(je.creditAmount)
                )
                FROM JournalEntry je
                JOIN je.transaction jt
                JOIN je.account a
                WHERE jt.transactionDate BETWEEN :startDate AND :endDate
                  AND a.status = 'ACTIVE'
                GROUP BY a.accountId, a.accountName, a.accountType
                ORDER BY a.accountId
            """)
    List<TrialBalanceRowDTO> findTrialBalanceRows(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);


}
