export enum AccountType {
    ASSET = 'ASSET',
    LIABILITY = 'LIABILITY',
    EQUITY = 'EQUITY',
    REVENUE = 'REVENUE',
    EXPENSE = 'EXPENSE',
}

export enum CashSource {
    NONE = 'NONE',
    CFI = 'INVESTING',
    CFO = 'OPERATING',
    CFF = 'FINANCING',
}

export enum AccountStatus {
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE',
}

export enum ProfitLossMapping {
    NONE = 'NONE',
    REVENUE = 'REVENUE',
    COGS = 'COGS',
    OPEX = 'OPEX',
    OTHER_INCOME = 'OTHER_INCOME',
    OTHER_EXPENSE = 'OTHER_EXPENSE',
}

export interface iAccount {
    accountPK?: number;
    accountId: string;
    accountName: string;
    accountType: AccountType;
    plMapping: ProfitLossMapping;
    cashSource: CashSource;
    isCashAccount: boolean;
    isCurrent: boolean;
    description: string;
    status: AccountStatus;
    hasTransactions: boolean;
    updatedAt?: string;
}

export interface iCreateAccount {
    accountId: string;
    accountName: string;
    accountType: AccountType;
    plMapping?: ProfitLossMapping;
    cashSource?: CashSource;
    isCashAccount?: boolean;
    isCurrent?: boolean;
    description?: string;
}

export interface iUpdateAccount {
    accountId?: string;
    accountName?: string;
    accountType?: AccountType;
    plMapping?: ProfitLossMapping;
    cashSource?: CashSource;
    isCashAccount?: boolean;
    isCurrent?: boolean;
    description?: string;
}
