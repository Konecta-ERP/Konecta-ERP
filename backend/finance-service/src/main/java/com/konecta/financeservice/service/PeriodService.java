package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.CreatePeriodDTO;
import com.konecta.financeservice.dto.PeriodDTO;
import com.konecta.financeservice.entity.JournalEntry;
import com.konecta.financeservice.entity.JournalTransaction;
import com.konecta.financeservice.entity.Period;
import com.konecta.financeservice.model.enums.PeriodStatus;
import com.konecta.financeservice.repository.JournalEntryRepository;
import com.konecta.financeservice.repository.JournalTransactionRepository;
import com.konecta.financeservice.repository.PeriodRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeriodService {

    private final PeriodRepository periodRepository;

    private final JournalTransactionRepository journalTransactionRepository;

    private final JournalEntryRepository journalEntryRepository;

    @Autowired
    public PeriodService(PeriodRepository periodRepository, JournalTransactionRepository journalTransactionRepository, JournalEntryRepository journalEntryRepository) {
        this.periodRepository = periodRepository;
        this.journalTransactionRepository = journalTransactionRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional
    public PeriodDTO createPeriod(CreatePeriodDTO dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        if (periodRepository.countOverlappingPeriods(dto.getStartDate(), dto.getEndDate()) > 0) {
            throw new IllegalArgumentException("Period overlaps with already existing periods");
        }

        Period period = new Period();
        period.setLabel(dto.getLabel());
        period.setStartDate(dto.getStartDate());
        period.setEndDate(dto.getEndDate());
        period.setStatus(dto.getStatus());
        period.setCreatedByUserId(dto.getCreatedByUserId());

        if (dto.getRevenueBudget() != null) period.setRevenueBudget(dto.getRevenueBudget());
        if (dto.getOpexBudget() != null) period.setCogsBudget(dto.getCogsBudget());
        if (dto.getOpexBudget() != null) period.setOpexBudget(dto.getOpexBudget());
        if (dto.getOtherIncomeBudget() != null) period.setOtherIncomeBudget(dto.getOtherIncomeBudget());
        if (dto.getOtherExpenseBudget() != null) period.setOtherExpenseBudget(dto.getOtherExpenseBudget());

        Period savedPeriod = periodRepository.save(period);
        return convertToDTO(savedPeriod);
    }

    public List<PeriodDTO> getAllPeriods() {
        return periodRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PeriodDTO> getLastSixPeriods() {
        return periodRepository.findTop6ByOrderByEndDateDesc().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public PeriodDTO startClosingPeriod(Long id) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Period with id " + id + "does not exist"));
        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalArgumentException("Period must be open to start closing");
        }

        BigDecimal totalDebit = BigDecimal.ZERO, totalCredit = BigDecimal.ZERO;
        List<JournalTransaction> transactions = journalTransactionRepository.findByPeriod_PeriodId(id);
        for (JournalTransaction jt : transactions) {
            List<JournalEntry> entries = journalEntryRepository.findAllByTransaction_TransactionId(jt.getTransactionId());
            for (JournalEntry e : entries) {
                totalDebit = totalDebit.add(e.getDebitAmount());
                totalCredit = totalCredit.add(e.getCreditAmount());
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException("Cannot close unbalanced period");
        }

        period.setStatus(PeriodStatus.CLOSING);
        Period savedPeriod = periodRepository.save(period);
        return convertToDTO(savedPeriod);
    }

    @Transactional
    public PeriodDTO lockPeriod(Long id) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Period with id " + id + "does not exist"));
        if (period.getStatus() != PeriodStatus.CLOSING) {
            throw new IllegalArgumentException("Period should first be closing before being locked");
        }

        period.setStatus(PeriodStatus.CLOSED);
        period.setClosedAt(LocalDateTime.now());
        period.setTimeToClose(ChronoUnit.DAYS.between(period.getCreatedAt().toLocalDate(), period.getClosedAt().toLocalDate()));
        Period savedPeriod = periodRepository.save(period);

        return convertToDTO(savedPeriod);
    }

    public PeriodDTO convertToDTO(Period period) {
        PeriodDTO dto = new PeriodDTO();
        dto.setPeriodId(period.getPeriodId());
        dto.setLabel(period.getLabel());
        dto.setStartDate(period.getStartDate());
        dto.setEndDate(period.getEndDate());
        dto.setStatus(period.getStatus());
        dto.setCreatedByUserId(period.getCreatedByUserId());
        dto.setCreatedAt(period.getCreatedAt());
        dto.setClosedAt(period.getClosedAt());
        dto.setTimeToClose(period.getTimeToClose());
        dto.setRevenueBudget(period.getRevenueBudget());
        dto.setCogsBudget(period.getCogsBudget());
        dto.setOpexBudget(period.getOpexBudget());
        dto.setOtherIncomeBudget(period.getOtherIncomeBudget());
        dto.setOtherExpenseBudget(period.getOtherExpenseBudget());
        return dto;
    }
}
