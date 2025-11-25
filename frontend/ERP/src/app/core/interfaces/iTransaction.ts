export interface iJournalTransaction {
    transactionId: number;
    transactionDate: string; // ISO Date
    description: string;
    postedByUserId: string;
    totalAmount?: number; // Optional helper for display
    entries?: iJournalEntry[];
}

export interface iCreateJournalTransaction {
    transactionDate: string; // YYYY-MM-DD
    description: string;
    postedByUserId: string;
    entries: iCreateJournalEntry[];
}

export interface iCreateJournalEntry {
    accountPK: number;
    debitAmount: number;
    creditAmount: number;
}

export interface iJournalEntry {
    entryId?: number;
    accountName?: string; // For display
    accountPK: number;
    debitAmount: number;
    creditAmount: number;
}
