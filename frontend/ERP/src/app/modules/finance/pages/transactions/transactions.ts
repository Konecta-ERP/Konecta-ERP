import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TransactionService } from '../../../../core/services/transaction.service';
import { AccountService } from '../../../../core/services/account.service';
import { UserService } from '../../../../core/services/user.service';
import {
    iJournalTransaction,
    iCreateJournalEntry,
    iCreateJournalTransaction,
} from '../../../../core/interfaces/iTransaction';
import { iAccount, AccountStatus } from '../../../../core/interfaces/iAccount';

@Component({
    selector: 'app-transactions',
    standalone: true,
    imports: [SharedModule],
    providers: [MessageService, ConfirmationService],
    templateUrl: './transactions.html',
    styleUrls: ['./transactions.css'],
})
export class Transactions implements OnInit {
    transactions: iJournalTransaction[] = [];
    allTransactions: iJournalTransaction[] = []; // Store full list for filtering
    accounts: iAccount[] = [];

    // Form Data
    transactionDate: Date | undefined;
    description: string = '';
    journalEntries: iCreateJournalEntry[] = [];

    // Filter Data
    filterStartDate: Date | undefined;
    filterEndDate: Date | undefined;

    // UI State
    transactionDialog: boolean = false;
    viewDetailsDialog: boolean = false;
    selectedTransaction: iJournalTransaction = {} as iJournalTransaction;

    submitted: boolean = false;
    loading: boolean = true;
    canCreate: boolean = false;

    constructor(
        private transactionService: TransactionService,
        private accountService: AccountService,
        private userService: UserService,
        private messageService: MessageService
    ) {}

    ngOnInit() {
        this.checkPermissions();
        this.loadData();
    }

    checkPermissions() {
        const user = this.userService.getUser();
        this.canCreate = user?.role === 'ACCOUNTANT' || user?.role === 'CFO';
    }

    loadData() {
        this.loading = true;

        this.transactionService.getAllTransactions().subscribe({
            next: (res: any) => {
                this.allTransactions = res.data || [];
                this.applyFilter(); // Apply filter immediately after loading
                this.loading = false;
            },
            error: () => (this.loading = false),
        });

        this.accountService.getAllAccounts().subscribe({
            next: (res: any) => {
                const allAccounts = res.data || [];
                this.accounts = allAccounts.filter(
                    (acc: iAccount) => acc.status === AccountStatus.ACTIVE
                );
            },
        });
    }

    applyFilter() {
        if (!this.filterStartDate && !this.filterEndDate) {
            this.transactions = [...this.allTransactions];
            return;
        }

        this.transactions = this.allTransactions.filter((tx) => {
            const txDate = new Date(tx.transactionDate);
            // Reset time part for accurate date comparison
            txDate.setHours(0, 0, 0, 0);

            let matchesStart = true;
            let matchesEnd = true;

            if (this.filterStartDate) {
                const start = new Date(this.filterStartDate);
                start.setHours(0, 0, 0, 0);
                matchesStart = txDate >= start;
            }

            if (this.filterEndDate) {
                const end = new Date(this.filterEndDate);
                end.setHours(0, 0, 0, 0);
                matchesEnd = txDate <= end;
            }

            return matchesStart && matchesEnd;
        });
    }

    clearFilter() {
        this.filterStartDate = undefined;
        this.filterEndDate = undefined;
        this.transactions = [...this.allTransactions];
    }

    openNew() {
        if (!this.canCreate) return;

        this.transactionDate = new Date();
        this.description = '';
        this.journalEntries = [
            { accountPK: 0, debitAmount: 0, creditAmount: 0 },
            { accountPK: 0, debitAmount: 0, creditAmount: 0 },
        ];

        this.submitted = false;
        this.transactionDialog = true;
    }

    viewDetails(transaction: iJournalTransaction) {
        this.selectedTransaction = transaction;
        this.viewDetailsDialog = true;
    }

    addLine() {
        this.journalEntries.push({ accountPK: 0, debitAmount: 0, creditAmount: 0 });
    }

    removeLine(index: number) {
        if (this.journalEntries.length > 2) {
            this.journalEntries.splice(index, 1);
        } else {
            this.messageService.add({
                severity: 'warn',
                summary: 'Warning',
                detail: 'A journal entry requires at least 2 lines.',
            });
        }
    }

    get totalDebits(): number {
        return this.journalEntries.reduce((sum, entry) => sum + (entry.debitAmount || 0), 0);
    }

    get totalCredits(): number {
        return this.journalEntries.reduce((sum, entry) => sum + (entry.creditAmount || 0), 0);
    }

    get isBalanced(): boolean {
        return Math.abs(this.totalDebits - this.totalCredits) < 0.01 && this.totalDebits > 0;
    }

    saveTransaction() {
        this.submitted = true;

        if (!this.transactionDate || !this.description) {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Date and Description are required',
            });
            return;
        }

        if (this.journalEntries.some((e) => !e.accountPK)) {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'All lines must have an Account selected',
            });
            return;
        }

        if (!this.isBalanced) {
            this.messageService.add({
                severity: 'error',
                summary: 'Unbalanced',
                detail: 'Total Debits must equal Total Credits',
            });
            return;
        }

        const user = this.userService.getUser();
        const userId = user?.id ? user.id : '';

        const payload: any = {
            transactionDate: this.formatDate(this.transactionDate),
            description: this.description,
            postedByUserId: userId,
            entries: this.journalEntries,
        };

        this.transactionService.createTransaction(payload).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Journal Posted Successfully',
                });
                this.transactionDialog = false;
                this.loadData();
            },
            error: (err) => {
                console.error(err);
                const serverMsg = err.error?.sMessage || 'Failed to post transaction';
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: serverMsg,
                });
            },
        });
    }

    formatDate(dateInput: any): string {
        if (!dateInput) return '';
        const date = new Date(dateInput);
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
}
