package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.CreateJournalEntryDTO;
import com.konecta.financeservice.dto.CreateJournalTransactionDTO;
import com.konecta.financeservice.dto.JournalEntryDTO;
import com.konecta.financeservice.dto.JournalTransactionDTO;
import com.konecta.financeservice.entity.Account;
import com.konecta.financeservice.entity.JournalEntry;
import com.konecta.financeservice.entity.JournalTransaction;
import com.konecta.financeservice.entity.Period;
import com.konecta.financeservice.model.enums.AccountStatus;
import com.konecta.financeservice.model.enums.PeriodStatus;
import com.konecta.financeservice.repository.AccountRepository;
import com.konecta.financeservice.repository.JournalEntryRepository;
import com.konecta.financeservice.repository.JournalTransactionRepository;
import com.konecta.financeservice.repository.PeriodRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JournalTransactionService {

    private final JournalTransactionRepository journalTransactionRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PeriodRepository periodRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public JournalTransactionService(JournalTransactionRepository journalTransactionRepository, JournalEntryRepository journalEntryRepository, PeriodRepository periodRepository, AccountRepository accountRepository) {
        this.journalTransactionRepository = journalTransactionRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.periodRepository = periodRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public JournalTransactionDTO createJournalTransaction(CreateJournalTransactionDTO dto) {
        Period period = periodRepository.findPeriodToMatchTransactionDate(dto.getTransactionDate())
                .orElseThrow(() -> new IllegalArgumentException("Cannot create a transaction at a date not covered by a period"));
        if (period.getStatus() == PeriodStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot create a transaction within a closed period");
        }

        JournalTransaction transaction = new JournalTransaction();
        transaction.setPeriod(period);
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setDescription(dto.getDescription());
        transaction.setPostedByUserId(dto.getPostedByUserId());
        JournalTransaction savedTransaction = journalTransactionRepository.save(transaction);

        List<JournalEntry> entriesToSave = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (CreateJournalEntryDTO entryDto : dto.getEntries()) {
            // load managed Account (throws if not found)
            Account account = accountRepository.findById(entryDto.getAccountPK())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found with PK: " + entryDto.getAccountPK()));

            if (account.getStatus() == AccountStatus.INACTIVE) {
                throw new IllegalArgumentException("An inactive account cannot be used for a transaction");
            }

            JournalEntry entry = new JournalEntry();
            entry.setTransaction(savedTransaction);
            entry.setAccount(account);

            if (entryDto.getDebitAmount() != null) {
                entry.setDebitAmount(entryDto.getDebitAmount());
                totalDebit = totalDebit.add(entryDto.getDebitAmount());
            }
            if (entryDto.getCreditAmount() != null) {
                entry.setCreditAmount(entryDto.getCreditAmount());
                totalCredit = totalCredit.add(entryDto.getCreditAmount());
            }

            entriesToSave.add(entry);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException("Total debit amount must equal total credit amount for a transaction");
        }

        List<JournalEntry> savedEntries = journalEntryRepository.saveAll(entriesToSave);

        Set<Account> accountsToUpdate = savedEntries.stream()
                .map(JournalEntry::getAccount)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Account acc : accountsToUpdate) {
            acc.setHasTransactions(true);
        }
        accountRepository.saveAll(new ArrayList<>(accountsToUpdate));

        return convertToDTO(savedTransaction, savedEntries);
    }

    public JournalTransactionDTO convertToDTO(JournalTransaction transaction, List<JournalEntry> entries) {
        JournalTransactionDTO dto = new JournalTransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setPeriodId(transaction.getPeriod().getPeriodId());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setEntries(new ArrayList<>());
        dto.setPostedByUserId(transaction.getPostedByUserId());
        dto.setCreatedAt(transaction.getCreatedAt());

        for (JournalEntry e : entries) {
            JournalEntryDTO entryDto = new JournalEntryDTO();
            entryDto.setEntryID(e.getEntryId());
            entryDto.setAccountPK(e.getAccount().getAccountPK());
            entryDto.setAccountId(e.getAccount().getAccountId());
            entryDto.setAccountName(e.getAccount().getAccountName());
            entryDto.setDebitAmount(e.getDebitAmount());
            entryDto.setCreditAmount(e.getCreditAmount());
            dto.getEntries().add(entryDto);
        }

        return dto;
    }

    public List<JournalTransactionDTO> getAllJournalTransactions() {
        List<JournalTransaction> transactions = journalTransactionRepository.findAll();
        
    
        return transactions.stream()
                .map(this::convertToDTOWithEntries)
                .collect(Collectors.toList());
    }

    // Helper to map Entity -> DTO
    private JournalTransactionDTO convertToDTOWithEntries(JournalTransaction transaction) {
        JournalTransactionDTO dto = new JournalTransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setPeriodId(transaction.getPeriod().getPeriodId());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setPostedByUserId(transaction.getPostedByUserId());
        dto.setCreatedAt(transaction.getCreatedAt());

    
        List<JournalEntryDTO> entryDTOs = new ArrayList<>();
        
        List<JournalEntry> entries = journalEntryRepository.findAllByTransaction_TransactionId(transaction.getTransactionId());

        for (JournalEntry e : entries) {
            JournalEntryDTO entryDto = new JournalEntryDTO();
            entryDto.setEntryID(e.getEntryId());
            entryDto.setAccountPK(e.getAccount().getAccountPK());
            entryDto.setAccountId(e.getAccount().getAccountId());
            entryDto.setAccountName(e.getAccount().getAccountName());
            entryDto.setDebitAmount(e.getDebitAmount());
            entryDto.setCreditAmount(e.getCreditAmount());
            entryDTOs.add(entryDto);
        }
        
        dto.setEntries(entryDTOs);
        return dto;
    }

}
