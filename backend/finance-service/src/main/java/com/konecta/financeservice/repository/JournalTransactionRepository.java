package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.JournalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalTransactionRepository extends JpaRepository<JournalTransaction, Long> {


    List<JournalTransaction> findByPeriod_PeriodId(Long periodId);
}
