package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.*;
import com.konecta.financeservice.entity.Account;
import com.konecta.financeservice.entity.Period;
import com.konecta.financeservice.model.enums.AccountType;
import com.konecta.financeservice.repository.AnalyticsRepository;
import com.konecta.financeservice.repository.PeriodRepository;
import com.konecta.financeservice.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final PeriodRepository periodRepository;
    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    @Autowired
    public AnalyticsService(AnalyticsRepository analyticsRepository, PeriodRepository periodRepository, AccountRepository accountRepository, EntityManager entityManager) {
        this.analyticsRepository = analyticsRepository;
        this.periodRepository = periodRepository;
        this.accountRepository = accountRepository;
        this.entityManager = entityManager;
    }

    public TrialBalanceReportDTO generateTrialBalance(Long periodId) {
        Period p = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        List<TrialBalanceRowDTO> rows = analyticsRepository.findTrialBalanceRows(p.getStartDate(), p.getEndDate());

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TrialBalanceRowDTO r : rows) {
            boolean debitNormal = true;
            if (r.getAccountType() == AccountType.LIABILITY
                    || r.getAccountType() == AccountType.EQUITY
                    || r.getAccountType() == AccountType.REVENUE) {
                debitNormal = false;
            }
            BigDecimal signed;
            if (debitNormal) {
                signed = r.getTotalDebits().subtract(r.getTotalCredits());
            } else {
                signed = r.getTotalCredits().subtract(r.getTotalDebits());
            }

            if (signed.compareTo(BigDecimal.ZERO) >= 0) {
                if (debitNormal) {
                    r.setDebitBalance(signed);
                    r.setCreditBalance(null);
                    totalDebits = totalDebits.add(signed);
                } else {
                    r.setCreditBalance(signed);
                    r.setDebitBalance(null);
                    totalCredits = totalCredits.add(signed);
                }
                r.setAbnormal(false);
            } else {
                // abnormal: opposite sign to normal
                BigDecimal abs = signed.abs();
                if (debitNormal) {
                    r.setCreditBalance(abs); // asset with credit balance — shown on credit column
                    r.setDebitBalance(null);
                    totalCredits = totalCredits.add(abs);
                } else {
                    r.setDebitBalance(abs);
                    r.setCreditBalance(null);
                    totalDebits = totalDebits.add(abs);
                }
                r.setAbnormal(true);
            }
        }

        String tbStatus = totalDebits.compareTo(totalCredits) == 0 ? "Balanced" : "Unbalanced";

        return new TrialBalanceReportDTO(p.getLabel(), p.getStatus().name(), rows, totalDebits, totalCredits, tbStatus);
    }

    public GLResponseDTO generateGLResponse(LocalDate fromDate, LocalDate toDate, List<Long> accountPKs) {
        if (accountPKs == null || accountPKs.isEmpty()) {
            List<Long> allAccountPKs = accountRepository.findAllActiveAccountPKs();
            accountPKs = allAccountPKs;
        }
        List<OpeningBalanceDTO> openings = fetchOpeningBalances(fromDate, accountPKs);
        List<GLRowDTO> entries = fetchEntries(fromDate, toDate, accountPKs);
        List<GLRowDTO> withRunning = computeRunningBalances(openings, entries);
        List<String> accountIds = analyticsRepository.findAllById(accountPKs)
                .stream()
                .map(Account::getAccountId)
                .toList();

        GLResponseDTO dto = new GLResponseDTO(fromDate, toDate, accountIds, withRunning);
        return dto;
    }

    public IncomeStatementDTO generateIncomeStatement(Long periodId) {
        Period p = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));

        String sql = """
                SELECT
                  COALESCE(SUM(CASE WHEN a.pl_mapping = 'REVENUE' THEN (je.credit_amount - je.debit_amount) END), 0) AS revenue,
                  COALESCE(SUM(CASE WHEN a.pl_mapping = 'COGS' THEN (je.debit_amount - je.credit_amount) END), 0) AS cogs,
                  COALESCE(SUM(CASE WHEN a.pl_mapping = 'OPEX' THEN (je.debit_amount - je.credit_amount) END), 0) AS opex,
                  COALESCE(SUM(CASE WHEN a.pl_mapping = 'OTHER_INCOME' THEN (je.credit_amount - je.debit_amount) END), 0) AS other_income,
                  COALESCE(SUM(CASE WHEN a.pl_mapping = 'OTHER_EXPENSE' THEN (je.debit_amount - je.credit_amount) END), 0) AS other_expense
                FROM journal_entries je
                JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id
                JOIN accounts a ON je.account_pk = a.account_pk
                WHERE jt.transaction_date BETWEEN ?1 AND ?2
                  AND a.status = 'ACTIVE'
                """;

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(p.getStartDate()));
        q.setParameter(2, java.sql.Date.valueOf(p.getEndDate()));

        Object[] row = (Object[]) q.getSingleResult();

        BigDecimal revenueActual = toBigDecimal(row[0]);
        BigDecimal cogsActual = toBigDecimal(row[1]);
        BigDecimal opexActual = toBigDecimal(row[2]);
        BigDecimal otherIncomeActual = toBigDecimal(row[3]);
        BigDecimal otherExpenseActual = toBigDecimal(row[4]);

        // budgets from period (null-safe)
        BigDecimal revenueBudget = nullSafe(p.getRevenueBudget());
        BigDecimal cogsBudget = nullSafe(p.getCogsBudget());
        BigDecimal opexBudget = nullSafe(p.getOpexBudget());
        BigDecimal otherIncomeBudget = nullSafe(p.getOtherIncomeBudget());
        BigDecimal otherExpenseBudget = nullSafe(p.getOtherExpenseBudget());

        // Derived actuals
        BigDecimal grossProfitActual = revenueActual.subtract(cogsActual);
        BigDecimal ebitActual = grossProfitActual.subtract(opexActual);
        BigDecimal netIncomeActual = ebitActual.add(otherIncomeActual).subtract(otherExpenseActual);

        // Derived budgets
        BigDecimal grossProfitBudget = revenueBudget.subtract(cogsBudget);
        BigDecimal ebitBudget = grossProfitBudget.subtract(opexBudget);
        BigDecimal netIncomeBudget = ebitBudget.add(otherIncomeBudget).subtract(otherExpenseBudget);

        // Variances
        BigDecimal revenueVariance = revenueActual.subtract(revenueBudget);
        BigDecimal grossProfitVariance = grossProfitActual.subtract(grossProfitBudget);
        BigDecimal ebitVariance = ebitActual.subtract(ebitBudget);
        BigDecimal netIncomeVariance = netIncomeActual.subtract(netIncomeBudget);
        BigDecimal cogsVariance = cogsActual.subtract(cogsBudget);

        // Percents (null if budget == 0)
        BigDecimal revenueVariancePct = computePct(revenueVariance, revenueBudget);
        BigDecimal grossProfitVariancePct = computePct(grossProfitVariance, grossProfitBudget);
        BigDecimal ebitVariancePct = computePct(ebitVariance, ebitBudget);
        BigDecimal netIncomeVariancePct = computePct(netIncomeVariance, netIncomeBudget);

        // Build DTO
        IncomeStatementDTO dto = IncomeStatementDTO.builder()
                .periodId(p.getPeriodId())
                .periodLabel(p.getLabel())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())

                .revenueActual(revenueActual)
                .cogsActual(cogsActual)
                .opexActual(opexActual)
                .otherIncomeActual(otherIncomeActual)
                .otherExpenseActual(otherExpenseActual)

                .revenueBudget(revenueBudget)
                .cogsBudget(cogsBudget)
                .opexBudget(opexBudget)
                .otherIncomeBudget(otherIncomeBudget)
                .otherExpenseBudget(otherExpenseBudget)

                .grossProfitActual(grossProfitActual)
                .ebitActual(ebitActual)
                .netIncomeActual(netIncomeActual)

                .grossProfitBudget(grossProfitBudget)
                .ebitBudget(ebitBudget)
                .netIncomeBudget(netIncomeBudget)

                .revenueVariance(revenueVariance)
                .cogsVariance(cogsVariance)
                .grossProfitVariance(grossProfitVariance)
                .ebitVariance(ebitVariance)
                .netIncomeVariance(netIncomeVariance)

                .revenueVariancePct(revenueVariancePct)
                .grossProfitVariancePct(grossProfitVariancePct)
                .ebitVariancePct(ebitVariancePct)
                .netIncomeVariancePct(netIncomeVariancePct)
                .build();

        return dto;
    }

    public BalanceSheetReportDTO generateBalanceSheet(LocalDate asOfDate) {
        String sql = """
                SELECT
                  a.account_pk,
                  a.account_id,
                  a.account_name,
                  a.account_type,
                  a.is_current,
                  COALESCE(SUM(je.debit_amount), 0) AS total_debits,
                  COALESCE(SUM(je.credit_amount), 0) AS total_credits
                FROM journal_entries je
                JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id
                JOIN accounts a ON je.account_pk = a.account_pk
                WHERE jt.transaction_date <= ?1
                  AND a.status = 'ACTIVE'
                GROUP BY a.account_pk, a.account_id, a.account_name, a.account_type, a.is_current
                ORDER BY a.account_type, a.is_current DESC, a.account_id
                """;

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(asOfDate));

        @SuppressWarnings("unchecked")
        List<Object[]> raw = q.getResultList();

        List<BalanceSheetAccountRowDTO> assetsCurrent = new ArrayList<>();
        List<BalanceSheetAccountRowDTO> assetsNonCurrent = new ArrayList<>();
        List<BalanceSheetAccountRowDTO> liabilitiesCurrent = new ArrayList<>();
        List<BalanceSheetAccountRowDTO> liabilitiesNonCurrent = new ArrayList<>();
        List<BalanceSheetAccountRowDTO> equityList = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (Object[] r : raw) {
            Long accountPk = ((Number) r[0]).longValue();
            String accountId = (String) r[1];
            String accountName = (String) r[2];
            String accountType = (String) r[3]; // "ASSET"/"LIABILITY"/"EQUITY"
            Boolean isCurrent = r[4] == null ? Boolean.FALSE : ((Boolean) r[4]);
            BigDecimal debits = toBigDecimal(r[5]);
            BigDecimal credits = toBigDecimal(r[6]);

            // compute signed balance according to account type
            BigDecimal signed;
            if ("ASSET".equalsIgnoreCase(accountType) || "EXPENSE".equalsIgnoreCase(accountType)) {
                signed = debits.subtract(credits); // debit-positive
            } else { // LIABILITY, EQUITY, REVENUE
                signed = credits.subtract(debits); // credit-positive
            }

            BalanceSheetAccountRowDTO row = new BalanceSheetAccountRowDTO();
            row.setAccountPk(accountPk);
            row.setAccountId(accountId);
            row.setAccountName(accountName);
            row.setAccountType(accountType);
            row.setIsCurrent(isCurrent);
            row.setTotalDebits(debits);
            row.setTotalCredits(credits);
            row.setSignedBalance(signed);

            // place row in correct bucket and accumulate totals using natural sign
            if ("ASSET".equalsIgnoreCase(accountType)) {
                if (isCurrent) {
                    assetsCurrent.add(row);
                } else {
                    assetsNonCurrent.add(row);
                }
                // assets total = sum(debit - credit) for assets
                totalAssets = totalAssets.add(signed);
            } else if ("LIABILITY".equalsIgnoreCase(accountType)) {
                if (isCurrent) {
                    liabilitiesCurrent.add(row);
                } else {
                    liabilitiesNonCurrent.add(row);
                }
                // liabilities total = sum(credit - debit)
                totalLiabilities = totalLiabilities.add(signed);
            } else if ("EQUITY".equalsIgnoreCase(accountType)) {
                equityList.add(row);
                totalEquity = totalEquity.add(signed);
            } else {
                // ignore revenue/expense accounts for balance sheet; or handle retained earnings mapping
            }
        }

        // Compute Validation Status: Assets ?= Liabilities + Equity
        BigDecimal liabilitiesPlusEquity = totalLiabilities.add(totalEquity);
        String status = totalAssets.compareTo(liabilitiesPlusEquity) == 0 ? "Balanced" : "Unbalanced";

        BalanceSheetReportDTO report = new BalanceSheetReportDTO();
        report.setAsOfDate(asOfDate);
        report.setAssetsCurrent(assetsCurrent);
        report.setAssetsNonCurrent(assetsNonCurrent);
        report.setLiabilitiesCurrent(liabilitiesCurrent);
        report.setLiabilitiesNonCurrent(liabilitiesNonCurrent);
        report.setEquity(equityList);
        report.setTotalAssets(totalAssets);
        report.setTotalLiabilities(totalLiabilities);
        report.setTotalEquity(totalEquity);
        report.setValidationStatus(status);

        return report;
    }

    public CashFlowReportDTO generateCashFlow(Long periodId) {
        Period p = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Period not found"));

        LocalDate start = p.getStartDate();
        LocalDate end = p.getEndDate();

        BigDecimal openingCash = getOpeningCash(start);
        Map<String, BigDecimal> sectionNet = getCashMovementsBySection(start, end);

        BigDecimal cfo = sectionNet.getOrDefault("CFO", BigDecimal.ZERO);
        BigDecimal cfi = sectionNet.getOrDefault("CFI", BigDecimal.ZERO);
        BigDecimal cff = sectionNet.getOrDefault("CFF", BigDecimal.ZERO);

        BigDecimal netChange = cfo.add(cfi).add(cff); // CFI/ CFF signs come from query
        BigDecimal endingCash = openingCash.add(netChange);

        BigDecimal bsCash = getBalanceSheetCash(end); // same logic as for opening but up to end

        boolean reconciled = endingCash.compareTo(bsCash) == 0;

        // build DTO
        CashFlowReportDTO dto = new CashFlowReportDTO();
        dto.setPeriodId(periodId);
        dto.setPeriodLabel(p.getLabel());
        dto.setOpeningCash(openingCash);
        dto.setCfo(cfo);
        dto.setCfi(cfi);
        dto.setCff(cff);
        dto.setNetChange(netChange);
        dto.setEndingCash(endingCash);
        dto.setBalanceSheetCash(bsCash);
        dto.setReconciled(reconciled);
        return dto;
    }

    private BigDecimal getOpeningCash(LocalDate start) {
        String sql = """
                  SELECT COALESCE(SUM(je.debit_amount - je.credit_amount), 0)
                  FROM journal_entries je
                  JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id
                  JOIN accounts a ON je.account_pk = a.account_pk
                  WHERE jt.transaction_date < ?1
                    AND a.is_cash_account = TRUE
                """;
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(start));
        return toBigDecimal(q.getSingleResult());
    }

    private Map<String, BigDecimal> getCashMovementsBySection(LocalDate start, LocalDate end) {
        String sql = """
                  SELECT COALESCE(other.cash_source, 'NONE') AS cash_section,
                         SUM(
                           CASE
                             WHEN cash_line.debit_amount > 0 THEN cash_line.debit_amount
                             WHEN cash_line.credit_amount > 0 THEN -cash_line.credit_amount
                             ELSE 0
                           END
                         ) AS net_cash
                  FROM journal_transactions jt
                  JOIN journal_entries cash_line ON jt.transaction_id = cash_line.transaction_id
                  JOIN accounts cash_acc ON cash_line.account_pk = cash_acc.account_pk
                    AND cash_acc.is_cash_account = TRUE
                  JOIN journal_entries other_line ON other_line.transaction_id = jt.transaction_id
                    AND other_line.entry_id <> cash_line.entry_id
                  JOIN accounts other ON other_line.account_pk = other.account_pk
                  WHERE jt.transaction_date BETWEEN ?1 AND ?2
                  GROUP BY COALESCE(other.cash_source, 'NONE')
                """;
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(start));
        q.setParameter(2, java.sql.Date.valueOf(end));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            String section = r[0] == null ? "NONE" : r[0].toString();
            BigDecimal v = toBigDecimal(r[1]);
            map.put(section, v);
        }
        return map;
    }

    private BigDecimal getBalanceSheetCash(LocalDate asOf) {
        String sql = """
                  SELECT COALESCE(SUM(je.debit_amount - je.credit_amount), 0)
                  FROM journal_entries je
                  JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id
                  JOIN accounts a ON je.account_pk = a.account_pk
                  WHERE jt.transaction_date <= ?1
                    AND a.is_cash_account = TRUE
                """;
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(asOf));
        return toBigDecimal(q.getSingleResult());
    }

    private List<GLRowDTO> fetchEntries(LocalDate fromDate, LocalDate toDate, List<Long> accountPKs) {
        String sql = "SELECT je.entry_id, jt.transaction_id, jt.transaction_date, je.account_pk, a.account_name, " +
                "je.debit_amount, je.credit_amount, (je.debit_amount - je.credit_amount) AS signed_amount, jt.description " +
                "FROM journal_entries je JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id " +
                "JOIN accounts a ON je.account_pk = a.account_pk " +
                "WHERE jt.transaction_date BETWEEN :fromDate AND :toDate AND a.status = 'ACTIVE' " +
                (accountPKs != null ? " AND je.account_pk IN (:accountPKs) " : "") +
                "ORDER BY je.account_pk, jt.transaction_date, jt.transaction_id, je.entry_id";

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("fromDate", fromDate);
        q.setParameter("toDate", toDate);
        if (accountPKs != null) q.setParameter("accountPKs", accountPKs);

        List<Object[]> raw = q.getResultList();
        List<GLRowDTO> rows = new ArrayList<>();
        for (Object[] r : raw) {
            GLRowDTO dto = new GLRowDTO();
            dto.setEntryId(((Number) r[0]).longValue());
            dto.setTransactionId(((Number) r[1]).longValue());
            dto.setTransactionDate(((java.sql.Date) r[2]).toLocalDate());
            dto.setAccountPK(((Number) r[3]).longValue());
            dto.setAccountName((String) r[4]);
            dto.setDebitAmount(toBigDecimal(r[5]));
            dto.setCreditAmount(toBigDecimal(r[6]));
            dto.setSignedAmount(toBigDecimal(r[7]));
            dto.setDescription((String) r[8]);
            rows.add(dto);
        }
        return rows;
    }

    private List<OpeningBalanceDTO> fetchOpeningBalances(LocalDate fromDate, List<Long> accountPKs) {
        String sql = "SELECT je.account_pk, SUM(je.debit_amount - je.credit_amount) AS opening_balance " +
                "FROM journal_entries je JOIN journal_transactions jt ON je.transaction_id = jt.transaction_id " +
                "WHERE jt.transaction_date < :fromDate " +
                (accountPKs != null ? " AND je.account_pk IN (:accountPKs) " : "") +
                "GROUP BY je.account_pk";

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("fromDate", fromDate);
        if (accountPKs != null) q.setParameter("accountPKs", accountPKs);
        List<Object[]> raw = q.getResultList();

        List<OpeningBalanceDTO> out = new ArrayList<>();
        for (Object[] r : raw) {
            out.add(new OpeningBalanceDTO(((Number) r[0]).longValue(), toBigDecimal(r[1])));
        }
        return out;
    }

    private List<GLRowDTO> computeRunningBalances(List<OpeningBalanceDTO> openings, List<GLRowDTO> entries) {
        Map<Long, BigDecimal> openingMap = openings.stream()
                .collect(Collectors.toMap(OpeningBalanceDTO::getAccountPK, OpeningBalanceDTO::getOpeningBalance));

        List<GLRowDTO> out = new ArrayList<>();
        Long currentAccount = null;
        BigDecimal running = BigDecimal.ZERO;

        for (GLRowDTO row : entries) {
            if (!row.getAccountPK().equals(currentAccount)) {
                currentAccount = row.getAccountPK();
                running = openingMap.getOrDefault(currentAccount, BigDecimal.ZERO);
            }
            // update running by signed amount (debit - credit)
            running = running.add(row.getSignedAmount());
            row.setRunningBalance(running);
            out.add(row);
        }
        return out;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return new BigDecimal(val.toString());
    }

    private BigDecimal computePct(BigDecimal variance, BigDecimal budget) {
        if (budget == null || BigDecimal.ZERO.compareTo(budget) == 0) return null;
        return variance.divide(budget, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
