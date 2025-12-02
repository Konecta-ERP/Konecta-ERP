import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AccountService } from '../../../../core/services/account.service'; // Service path as per your snippet
import { UserService } from '../../../../core/services/user.service'; // Import UserService
import {
    iAccount,
    iCreateAccount,
    iUpdateAccount,
    AccountStatus,
    AccountType,
    CashSource,
    ProfitLossMapping,
} from '../../../../core/interfaces/iAccount';
import { isAdmin } from '../../../../core/constants/roles';

@Component({
    selector: 'app-accounts',
    imports: [SharedModule],
    providers: [MessageService, ConfirmationService],
    templateUrl: './accounts.html',
    styleUrl: './accounts.css',
})
export class Accounts implements OnInit {
    accounts: iAccount[] = [];
    account: iAccount = {} as iAccount;
    selectedAccounts: iAccount[] = [];

    // UI State
    accountDialog: boolean = false;
    submitted: boolean = false;
    loading: boolean = true;

    // Permissions
    isCFO: boolean = false;

    // Dropdown Options
    accountTypes = Object.values(AccountType).map((key) => ({ label: key, value: key }));
    plMappings = Object.values(ProfitLossMapping).map((key) => ({ label: key, value: key }));
    cashSources = Object.values(CashSource).map((key) => ({ label: key, value: key }));
    statuses = Object.values(AccountStatus).map((key) => ({ label: key, value: key }));

    constructor(
        private messageService: MessageService,
        private confirmationService: ConfirmationService,
        private accountService: AccountService,
        private userService: UserService // Inject UserService
    ) {}

    ngOnInit() {
        this.checkPermissions();
        this.loadAccounts();
    }

    /**
     * Check if the current user has CFO privileges
     */
    checkPermissions() {
        const user = this.userService.getUser();
        this.isCFO = user?.role === 'CFO' || isAdmin(user?.role);
    }

    /**
     * Fetch all accounts from the backend
     */
    loadAccounts() {
        this.loading = true;
        this.accountService.getAllAccounts().subscribe({
            next: (response: any) => {
                // Assuming the API returns a wrapper with a 'data' property
                this.accounts = response.data || [];
                this.loading = false;
            },
            error: (error) => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: error.error.cMessage,
                });
                console.error('Load Error:', error);
            },
        });
    }

    openNew() {
        // Optional: Guard logic
        if (!this.isCFO) {
            this.messageService.add({
                severity: 'error',
                summary: 'Unauthorized',
                detail: 'Only CFO can create accounts',
            });
            return;
        }

        this.account = {
            accountPK: -1, // Default for new
            accountId: '',
            accountName: '',
            accountType: AccountType.ASSET,
            plMapping: ProfitLossMapping.NONE,
            cashSource: CashSource.NONE,
            status: AccountStatus.ACTIVE,
            isCashAccount: false,
            isCurrent: true,
            hasTransactions: false,
            description: '',
        } as iAccount;
        this.submitted = false;
        this.accountDialog = true;
    }

    editAccount(account: iAccount) {
        // Optional: Guard logic
        if (!this.isCFO) {
            this.messageService.add({
                severity: 'error',
                summary: 'Unauthorized',
                detail: 'Only CFO can edit accounts',
            });
            return;
        }
        this.account = { ...account }; // Create a copy
        this.accountDialog = true;
    }

    saveAccount() {
        this.submitted = true;

        if (this.account.accountId && this.account.accountName) {
            // Check if we are updating (has PK) or creating (no PK or < 0)
            if (this.account.accountPK && this.account.accountPK >= 0) {
                // --- UPDATE ---
                const updateData: iUpdateAccount = {
                    accountName: this.account.accountName,
                    ...(this.account.hasTransactions === false
                        ? {
                              accountType: this.account.accountType,
                              accountId: this.account.accountId,
                          }
                        : {}),
                    plMapping: this.account.plMapping,
                    cashSource: this.account.cashSource,
                    isCashAccount: this.account.isCashAccount,
                    isCurrent: this.account.isCurrent,
                    description: this.account.description,
                };

                this.accountService
                    .updateAccount(this.account.accountPK || 0, updateData)
                    .subscribe({
                        next: (response: any) => {
                            this.messageService.add({
                                severity: 'success',
                                summary: 'Successful',
                                detail: response.cMessage,
                            });
                            this.loadAccounts();
                            this.accountDialog = false;
                            this.account = {} as iAccount;
                        },
                        error: (error) => {
                            this.messageService.add({
                                severity: 'error',
                                summary: 'Error',
                                detail: error.error.cMessage,
                            });
                        },
                    });
            } else {
                // --- CREATE ---
                const createData: iCreateAccount = {
                    accountId: this.account.accountId,
                    accountName: this.account.accountName,
                    accountType: this.account.accountType,
                    plMapping: this.account.plMapping,
                    cashSource: this.account.cashSource,
                    isCashAccount: this.account.isCashAccount,
                    isCurrent: this.account.isCurrent,
                    description: this.account.description,
                };

                this.accountService.createAccount(createData).subscribe({
                    next: (response: any) => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Successful',
                            detail: response.cMessage,
                        });
                        this.loadAccounts();
                        this.accountDialog = false;
                        this.account = {} as iAccount;
                    },
                    error: (error) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: error.error.cMessage,
                        });
                    },
                });
            }
        }
    }

    /**
     * Toggle Status (Deactivate)
     */
    toggleStatus(account: iAccount) {
        // Optional: Guard logic
        if (!this.isCFO) {
            this.messageService.add({
                severity: 'error',
                summary: 'Unauthorized',
                detail: 'Only CFO can change status',
            });
            return;
        }

        const isCurrentlyActive = account.status === AccountStatus.ACTIVE;
        const action = isCurrentlyActive ? 'deactivate' : 'activate';

        // Guard against missing PK
        if (!account.accountPK) {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Account ID missing',
            });
            return;
        }

        this.confirmationService.confirm({
            message: `Are you sure you want to ${action} ${account.accountName}?`,
            header: 'Confirm Status Change',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                // Call Deactivate Service
                if (isCurrentlyActive) {
                    this.accountService.deactivateAccount(account.accountPK || 0).subscribe({
                        next: (response: any) => {
                            account.status = AccountStatus.INACTIVE;
                            this.messageService.add({
                                severity: 'success',
                                summary: 'Successful',
                                detail: `Account ${action}d`,
                            });
                        },
                        error: (error) => {
                            this.messageService.add({
                                severity: 'error',
                                summary: 'Error',
                                detail: `Failed to ${action} account`,
                            });
                        },
                    });
                } else {
                    this.accountService.deactivateAccount(account.accountPK || 0).subscribe({
                        next: (response: any) => {
                            account.status = AccountStatus.ACTIVE;
                            this.messageService.add({
                                severity: 'success',
                                summary: 'Successful',
                                detail: 'Account Activated',
                            });
                        },
                        error: (err) => {
                            this.messageService.add({
                                severity: 'warn',
                                summary: 'Warning',
                                detail: `Failed to ${action} account`,
                            });
                        },
                    });
                }
            },
        });
    }

    findIndexById(id: number): number {
        return this.accounts.findIndex((acc) => acc.accountPK === id);
    }

    getSeverity(status: string) {
        return status === 'ACTIVE' ? 'success' : 'danger';
    }
}
