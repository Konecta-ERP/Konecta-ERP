package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findAllByTransaction_TransactionId(Long transactionId);
}
