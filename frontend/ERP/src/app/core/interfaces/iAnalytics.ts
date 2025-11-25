export interface iBalanceSheetAccountRow {
    accountPk: number;
    accountId: string;
    accountName: string;
    accountType: string; // "ASSET" | "LIABILITY" | "EQUITY"
    isCurrent: boolean;
    totalDebits: number;
    totalCredits: number;
    signedBalance: number;
}

export interface iBalanceSheetReport {
    asOfDate: string;
    assetsCurrent: iBalanceSheetAccountRow[];
    assetsNonCurrent: iBalanceSheetAccountRow[];
    liabilitiesCurrent: iBalanceSheetAccountRow[];
    liabilitiesNonCurrent: iBalanceSheetAccountRow[];
    equity: iBalanceSheetAccountRow[];

    totalAssets: number;
    totalLiabilities: number;
    totalEquity: number;
    validationStatus: 'Balanced' | 'Unbalanced';
}

export interface iGLRow {
    entryId: number;
    transactionId: number;
    transactionDate: string;
    accountPK: number;
    accountName: string;
    debitAmount: number;
    creditAmount: number;
    signedAmount: number;
    runningBalance: number;
    description: string;
}

export interface iGLResponse {
    fromDate: string;
    toDate: string;
    filteredAccounts: string[];
    entries: iGLRow[];
}
