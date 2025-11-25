export enum PeriodStatus {
    OPEN = 'OPEN',
    CLOSING = 'CLOSING',
    CLOSED = 'CLOSED',
}

export interface iPeriod {
    periodId: number;
    label: string;
    startDate: string;
    endDate: string;
    status: PeriodStatus;
    createdByUserId: string;
    createdAt?: string;
    closedAt?: string;
    timeToClose?: number;

    revenueBudget?: number;
    cogsBudget?: number;
    opexBudget?: number;
    otherIncomeBudget?: number;
    otherExpenseBudget?: number;
}

export interface iCreatePeriod {
    label: string;
    startDate: string;
    endDate: string;
    status: PeriodStatus;
    createdByUserId: string;

    revenueBudget?: number;
    cogsBudget?: number;
    opexBudget?: number;
    otherIncomeBudget?: number;
    otherExpenseBudget?: number;
}

// --- Trial Balance Interfaces ---
export interface iTrialBalanceRow {
    accountId: string;
    accountName: string;
    accountType: string;
    totalDebits: number;
    totalCredits: number;
    debitBalance: number;
    creditBalance: number;
    abnormal: boolean;
}

export interface iTrialBalanceReport {
    periodLabel: string;
    periodStatus: string;
    rows: iTrialBalanceRow[];
    totalDebits: number;
    totalCredits: number;
    tbStatus: 'Balanced' | 'Unbalanced';
}

// --- Income Statement Interfaces ---
export interface iIncomeStatement {
    periodId: number;
    periodLabel: string;
    startDate: string;
    endDate: string;

    // Actuals
    revenueActual: number;
    cogsActual: number;
    opexActual: number;
    otherIncomeActual: number;
    otherExpenseActual: number;

    // Budgets
    revenueBudget: number;
    cogsBudget: number;
    opexBudget: number;
    otherIncomeBudget: number;
    otherExpenseBudget: number;

    // Derived Actuals
    grossProfitActual: number;
    ebitActual: number;
    netIncomeActual: number;

    // Variances
    revenueVariance: number;
    cogsVariance: number;
    grossProfitVariance: number;
    ebitVariance: number;
    netIncomeVariance: number;

    // Variance %
    revenueVariancePct: number;
    grossProfitVariancePct: number;
    ebitVariancePct: number;
    netIncomeVariancePct: number;
}

// --- Cash Flow Interfaces ---
export interface iCashFlowReport {
    periodId: number;
    periodLabel: string;
    openingCash: number;
    cfo: number; // Operating
    cfi: number; // Investing
    cff: number; // Financing
    netChange: number;
    endingCash: number;
    balanceSheetCash: number;
    reconciled: boolean;
    sectionDetails?: { [key: string]: number };
}
