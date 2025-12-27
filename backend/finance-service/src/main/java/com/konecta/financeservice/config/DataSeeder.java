package com.konecta.financeservice.config;

import com.konecta.financeservice.entity.Account;
import com.konecta.financeservice.entity.JournalEntry;
import com.konecta.financeservice.entity.JournalTransaction;
import com.konecta.financeservice.entity.Period;
import com.konecta.financeservice.model.enums.*;
import com.konecta.financeservice.repository.AccountRepository;
import com.konecta.financeservice.repository.JournalEntryRepository;
import com.konecta.financeservice.repository.JournalTransactionRepository;
import com.konecta.financeservice.repository.PeriodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PeriodRepository periodRepository;
    private final JournalTransactionRepository transactionRepository;
    private final JournalEntryRepository entryRepository;

    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

    public DataSeeder(AccountRepository accountRepository,
                      PeriodRepository periodRepository,
                      JournalTransactionRepository transactionRepository,
                      JournalEntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.periodRepository = periodRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("\n--- Starting Finance Service Data Seeder ---");

        if (accountRepository.count() > 0) {
            System.out.println("Data already exists. Skipping seed.");
            return;
        }

        System.out.println("\n--- Creating Financial Periods (2025) ---");

        // 1. Create Historical Closed Periods (Jan - June)
        createPeriod("January 2025", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), PeriodStatus.CLOSED);
        createPeriod("February 2025", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28), PeriodStatus.CLOSED);
        createPeriod("March 2025", LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), PeriodStatus.CLOSED);
        createPeriod("April 2025", LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30), PeriodStatus.CLOSED);
        createPeriod("May 2025", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), PeriodStatus.CLOSED);
        createPeriod("June 2025", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), PeriodStatus.CLOSED);

        // 2. Create Current Active Period (July)
        // We capture this variable to assign the transactions below to this specific period
        Period julyPeriod = createPeriod("July 2025", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31), PeriodStatus.OPEN);

        // 3. Create Future Periods (Aug - Dec)
        createPeriod("August 2025", LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 31), PeriodStatus.OPEN);
        createPeriod("September 2025", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30), PeriodStatus.OPEN);
        createPeriod("October 2025", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31), PeriodStatus.OPEN);
        createPeriod("November 2025", LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 30), PeriodStatus.OPEN);
        createPeriod("December 2025", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31), PeriodStatus.OPEN);


        System.out.println("\n--- Creating Chart of Accounts ---");

        // ASSETS
        Account cash = createAccount("1000", "Cash", AccountType.ASSET, ProfitLossMapping.NONE, CashSource.CFO, true, true, AccountStatus.ACTIVE);
        Account accountsReceivable = createAccount("1100", "Accounts Receivable", AccountType.ASSET, ProfitLossMapping.NONE, CashSource.NONE, false, true, AccountStatus.ACTIVE);
        Account officeSupplies = createAccount("1200", "Office Supplies", AccountType.ASSET, ProfitLossMapping.NONE, CashSource.NONE, false, true, AccountStatus.ACTIVE);
        Account computerEquipment = createAccount("1500", "Computer Equipment", AccountType.ASSET, ProfitLossMapping.NONE, CashSource.CFI, false, false, AccountStatus.ACTIVE);
        Account investmentFund = createAccount("1600", "Strategic Investments", AccountType.ASSET, ProfitLossMapping.NONE, CashSource.CFF, false, false, AccountStatus.ACTIVE);

        // LIABILITIES
        Account accountsPayable = createAccount("2000", "Accounts Payable", AccountType.LIABILITY, ProfitLossMapping.NONE, CashSource.NONE, false, true, AccountStatus.ACTIVE);
        Account accruedExpenses = createAccount("2100", "Accrued Expenses", AccountType.LIABILITY, ProfitLossMapping.NONE, CashSource.NONE, false, true, AccountStatus.ACTIVE);
        Account oldLoan = createAccount("2500", "Old Bank Loan", AccountType.LIABILITY, ProfitLossMapping.NONE, CashSource.NONE, false, false, AccountStatus.INACTIVE);

        // EQUITY
        Account ownersCapital = createAccount("3000", "Owner's Capital", AccountType.EQUITY, ProfitLossMapping.NONE, CashSource.CFF, false, false, AccountStatus.ACTIVE);

        // REVENUE
        Account serviceRevenue = createAccount("4000", "Service Revenue", AccountType.REVENUE, ProfitLossMapping.REVENUE, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account interestIncome = createAccount("4100", "Interest Income", AccountType.REVENUE, ProfitLossMapping.OTHER_INCOME, CashSource.NONE, false, false, AccountStatus.ACTIVE);

        // EXPENSES
        Account rentExpense = createAccount("5000", "Rent Expense", AccountType.EXPENSE, ProfitLossMapping.OPEX, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account salariesExpense = createAccount("5100", "Salaries Expense", AccountType.EXPENSE, ProfitLossMapping.OPEX, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account advertisingExpense = createAccount("5200", "Advertising Expense", AccountType.EXPENSE, ProfitLossMapping.OPEX, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account utilitiesExpense = createAccount("5300", "Utilities Expense", AccountType.EXPENSE, ProfitLossMapping.OPEX, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account cogs = createAccount("5400", "Cost of Goods Sold", AccountType.EXPENSE, ProfitLossMapping.COGS, CashSource.NONE, false, false, AccountStatus.ACTIVE);
        Account penalties = createAccount("5900", "Penalties & Fines", AccountType.EXPENSE, ProfitLossMapping.OTHER_EXPENSE, CashSource.NONE, false, false, AccountStatus.ACTIVE);

        System.out.println("\n--- Posting Journal Entries (July 2025) ---");

        // Transaction 1: July 1 - Owner's Investment [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 1), "Owner's cash investment",
                new EntryRequest(cash, 50000.00, 0),
                new EntryRequest(ownersCapital, 0, 50000.00)
        );

        // Transaction 2: July 2 - Rent Payment [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 2), "Payment of office rent",
                new EntryRequest(rentExpense, 3000.00, 0),
                new EntryRequest(cash, 0, 3000.00)
        );

        // Transaction 3: July 3 - Purchase Supplies on Account [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 3), "Purchase of office supplies on account",
                new EntryRequest(officeSupplies, 800.00, 0),
                new EntryRequest(accountsPayable, 0, 800.00)
        );

        // Transaction 4: July 5 - Cash Received for Services [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 5), "Cash received for consulting services",
                new EntryRequest(cash, 7500.00, 0),
                new EntryRequest(serviceRevenue, 0, 7500.00)
        );

        // Transaction 5: July 8 - Purchase Equipment (Partial Cash/Credit) [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 8), "Purchase of computer equipment",
                new EntryRequest(computerEquipment, 15000.00, 0),
                new EntryRequest(cash, 0, 5000.00),
                new EntryRequest(accountsPayable, 0, 10000.00)
        );

        // Transaction 6: July 10 - Advertising Payment [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 10), "Payment for advertising",
                new EntryRequest(advertisingExpense, 400.00, 0),
                new EntryRequest(cash, 0, 400.00)
        );

        // Transaction 7: July 12 - Services on Credit [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 12), "Consulting services performed on credit",
                new EntryRequest(accountsReceivable, 6000.00, 0),
                new EntryRequest(serviceRevenue, 0, 6000.00)
        );

        // Transaction 8: July 15 - Salaries Payment [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 15), "Salary payment",
                new EntryRequest(salariesExpense, 4000.00, 0),
                new EntryRequest(cash, 0, 4000.00)
        );

        // Transaction 9: July 18 - Payment to Supplier [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 18), "Payment to supplier for previous purchase",
                new EntryRequest(accountsPayable, 800.00, 0),
                new EntryRequest(cash, 0, 800.00)
        );

        // Transaction 10: July 20 - Collection of AR [cite: 1]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 20), "Partial collection of accounts receivable",
                new EntryRequest(cash, 3000.00, 0),
                new EntryRequest(accountsReceivable, 0, 3000.00)
        );

        // Transaction 11: July 25 - Owner Withdrawal [cite: 9]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 25), "Owner's withdrawal for personal use",
                new EntryRequest(ownersCapital, 1500.00, 0),
                new EntryRequest(cash, 0, 1500.00)
        );

        // Transaction 12: July 28 - Accrual Expenses [cite: 9]
        createTransaction(julyPeriod, LocalDate.of(2025, 7, 28), "Utilities expense payable next month",
                new EntryRequest(utilitiesExpense, 600.00, 0),
                new EntryRequest(accruedExpenses, 0, 600.00)
        );

        System.out.println("--- Finance Data Seeder Complete ---");
    }

    // --- Helper Methods ---

    private Period createPeriod(String label, LocalDate start, LocalDate end, PeriodStatus status) {
        Period period = new Period();
        period.setLabel(label);
        period.setStartDate(start);
        period.setEndDate(end);
        period.setStatus(status);
        period.setCreatedByUserId(SYSTEM_USER_ID);

        // Simple default budgets for all seeded periods
        period.setRevenueBudget(new BigDecimal("15000.00"));
        period.setCogsBudget(new BigDecimal("2000.00"));
        period.setOpexBudget(new BigDecimal("8000.00"));

        return periodRepository.save(period);
    }

    private Account createAccount(String id, String name, AccountType type, ProfitLossMapping pl,
                                  CashSource source, boolean isCurrent, boolean isCash, AccountStatus status) {
        Account account = new Account();
        account.setAccountId(id);
        account.setAccountName(name);
        account.setAccountType(type);
        account.setPlMapping(pl);
        account.setCashSource(source);
        account.setCurrent(isCurrent);
        account.setCashAccount(isCash);
        account.setStatus(status);
        account.setHasTransactions(false);
        return accountRepository.save(account);
    }

    private void createTransaction(Period period, LocalDate date, String desc, EntryRequest... entries) {
        JournalTransaction tx = new JournalTransaction();
        tx.setPeriod(period);
        tx.setTransactionDate(date);
        tx.setDescription(desc);
        tx.setPostedByUserId(SYSTEM_USER_ID);

        JournalTransaction savedTx = transactionRepository.save(tx);

        for (EntryRequest req : entries) {
            JournalEntry entry = new JournalEntry();
            entry.setTransaction(savedTx);
            entry.setAccount(req.account);
            entry.setDebitAmount(BigDecimal.valueOf(req.debit));
            entry.setCreditAmount(BigDecimal.valueOf(req.credit));
            entryRepository.save(entry);

            if (!req.account.isHasTransactions()) {
                req.account.setHasTransactions(true);
                accountRepository.save(req.account);
            }
        }
    }

    private record EntryRequest(Account account, double debit, double credit) {
    }
}