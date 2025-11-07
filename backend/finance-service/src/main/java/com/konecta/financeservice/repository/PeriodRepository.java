package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodRepository extends JpaRepository<Period, Long> {

    /**
     * Checks if any period overlaps the given date range.
     * Use this for creating a new period.
     */
    @Query("""
                SELECT COUNT(p)
                FROM Period p
                WHERE p.startDate <= :endDate
                  AND p.endDate   >= :startDate
            """)
    long countOverlappingPeriods(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query("""
                SELECT p
                FROM Period p
                WHERE p.startDate <= :date
                  AND p.endDate   >= :date
            """)
    Optional<Period> findPeriodToMatchTransactionDate(@Param("date") LocalDate date);

    List<Period> findTop6ByOrderByEndDateDesc();

}
