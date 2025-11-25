import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { ConfirmationService, MessageService } from 'primeng/api';
import { PeriodService } from '../../../../core/services/period.service';
import { UserService } from '../../../../core/services/user.service';
import {
    iPeriod,
    iCreatePeriod,
    PeriodStatus,
    iCashFlowReport,
    iIncomeStatement,
    iTrialBalanceReport,
} from '../../../../core/interfaces/iPeriod';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-periods',
    standalone: true,
    imports: [SharedModule],
    providers: [MessageService, ConfirmationService],
    templateUrl: './periods.html',
    styleUrls: ['./periods.css'],
})
export class Periods implements OnInit {
    periods: iPeriod[] = [];
    lastSixPeriods: iPeriod[] = [];
    period: iCreatePeriod = {} as iCreatePeriod;

    // UI State
    periodDialog: boolean = false;
    viewBudgetDialog: boolean = false;
    submitted: boolean = false;

    // Report States
    tbDialog: boolean = false;
    tbReport: iTrialBalanceReport = {} as iTrialBalanceReport;

    incomeDialog: boolean = false;
    incomeReport: iIncomeStatement = {} as iIncomeStatement;

    cashFlowDialog: boolean = false;
    cashFlowReport: iCashFlowReport = {} as iCashFlowReport;

    loading: boolean = true;
    reportLoading: boolean = false;

    isCFO: boolean = false;
    isAccountant: boolean = false;

    // Chart Data
    chartData: any;
    chartOptions: any;

    // Checklist State
    checklistVisible: boolean = false;
    checklistSteps: any[] = [];
    activePeriodForClosing: iPeriod | null = null;
    selectedPeriodForBudgets: iPeriod = {} as iPeriod;
    selectedPeriodForReport: iPeriod = {} as iPeriod; // For export context

    constructor(
        private periodService: PeriodService,
        private userService: UserService,
        private messageService: MessageService,
        private confirmationService: ConfirmationService
    ) {}

    ngOnInit() {
        this.checkPermissions();
        this.loadData();
        this.initChartOptions();
    }

    checkPermissions() {
        const user = this.userService.getUser();
        this.isCFO = user?.role === 'CFO';
        this.isAccountant = user?.role === 'ACCOUNTANT';
    }

    loadData() {
        this.loading = true;
        this.periodService.getAllPeriods().subscribe({
            next: (res: any) => {
                this.periods = res.data || [];
                this.loading = false;
            },
            error: () => (this.loading = false),
        });

        this.periodService.getLastSixPeriods().subscribe({
            next: (res: any) => {
                this.lastSixPeriods = res.data || [];
                this.initChartData();
            },
        });
    }

    initChartData() {
        const labels = this.lastSixPeriods.map((p) => p.label);
        const data = this.lastSixPeriods.map((p) => p.timeToClose || 0);

        this.chartData = {
            labels: labels,
            datasets: [
                {
                    label: 'Time to Close (Days)',
                    data: data,
                    backgroundColor: '#4F46E5',
                    borderColor: '#4F46E5',
                    fill: false,
                    tension: 0.4,
                },
            ],
        };
    }

    initChartOptions() {
        this.chartOptions = {
            maintainAspectRatio: false,
            aspectRatio: 0.6,
            plugins: {
                legend: { labels: { color: '#495057' } },
            },
            scales: {
                x: { ticks: { color: '#495057' }, grid: { color: '#ebedef' } },
                y: { ticks: { color: '#495057' }, grid: { color: '#ebedef' } },
            },
        };
    }

    openNew() {
        if (!this.isCFO) return;
        const user = this.userService.getUser();
        this.period = {
            label: '',
            startDate: '',
            endDate: '',
            status: PeriodStatus.OPEN,
            createdByUserId: user?.id || '',
            revenueBudget: 0,
            cogsBudget: 0,
            opexBudget: 0,
            otherIncomeBudget: 0,
            otherExpenseBudget: 0,
        };
        this.submitted = false;
        this.periodDialog = true;
    }

    savePeriod() {
        this.submitted = true;
        if (this.period.label && this.period.startDate && this.period.endDate) {
            const payload: any = {
                ...this.period,
                startDate: this.formatDate(this.period.startDate),
                endDate: this.formatDate(this.period.endDate),
            };

            this.periodService.createPeriod(payload).subscribe({
                next: () => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Success',
                        detail: 'Period Created',
                    });
                    this.periodDialog = false;
                    this.loadData();
                },
                error: (err) => {
                    console.error(err);
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: 'Failed to create period',
                    });
                },
            });
        }
    }

    formatDate(dateInput: any): string {
        if (!dateInput) return '';
        const date = new Date(dateInput);
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    initiateClosing(period: iPeriod) {
        this.activePeriodForClosing = period;
        this.checklistSteps = [
            { label: 'Trial Balance Balanced', completed: true },
            { label: 'Sub-ledgers Reconciled', completed: true },
            { label: 'Pending Journals Posted', completed: true },
        ];
        this.checklistVisible = true;
    }

    confirmStartClosing() {
        if (!this.activePeriodForClosing) return;
        this.periodService.startClosingPeriod(this.activePeriodForClosing.periodId).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'info',
                    summary: 'Closing Started',
                    detail: 'Period is now in CLOSING state.',
                });
                this.checklistVisible = false;
                this.loadData();
            },
            error: () =>
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to start closing',
                }),
        });
    }

    lockPeriod(period: iPeriod) {
        if (!this.isCFO) {
            this.messageService.add({
                severity: 'error',
                summary: 'Unauthorized',
                detail: 'Only CFO can lock periods',
            });
            return;
        }
        this.confirmationService.confirm({
            message: `Are you sure you want to permanently LOCK ${period.label}? No further entries will be allowed.`,
            header: 'Confirm Period Lock',
            icon: 'pi pi-lock',
            accept: () => {
                this.periodService.lockPeriod(period.periodId).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Period Locked',
                            detail: 'Period is now CLOSED.',
                        });
                        this.loadData();
                    },
                    error: () =>
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: 'Failed to lock period',
                        }),
                });
            },
        });
    }

    viewBudgets(period: iPeriod) {
        this.selectedPeriodForBudgets = period;
        this.viewBudgetDialog = true;
    }

    // --- Reports Handlers ---

    viewTrialBalance(period: iPeriod) {
        this.selectedPeriodForReport = period;
        this.reportLoading = true;
        this.tbDialog = true;
        this.periodService.getTrialBalanceReport(period.periodId).subscribe({
            next: (res: any) => {
                this.tbReport = res.data;
                this.reportLoading = false;
            },
            error: () => {
                this.reportLoading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load Trial Balance',
                });
            },
        });
    }

    viewIncomeStatement(period: iPeriod) {
        this.selectedPeriodForReport = period;
        this.reportLoading = true;
        this.incomeDialog = true;
        this.periodService.getIncomeStatement(period.periodId).subscribe({
            next: (res: any) => {
                this.incomeReport = res.data;
                this.reportLoading = false;
            },
            error: () => {
                this.reportLoading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load Income Statement',
                });
            },
        });
    }

    viewCashFlow(period: iPeriod) {
        this.selectedPeriodForReport = period;
        this.reportLoading = true;
        this.cashFlowDialog = true;
        this.periodService.getCashFlowReport(period.periodId).subscribe({
            next: (res: any) => {
                this.cashFlowReport = res.data;
                this.reportLoading = false;
            },
            error: () => {
                this.reportLoading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load Cash Flow',
                });
            },
        });
    }

    // --- Generic Export Logic ---
    downloadFile(data: Blob, filename: string) {
        const url = window.URL.createObjectURL(data);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = filename;
        anchor.click();
        window.URL.revokeObjectURL(url);
    }

    exportPDF(type: 'tb' | 'is' | 'cf') {
        const id = this.selectedPeriodForReport.periodId;
        if (!id) return;

        this.messageService.add({
            severity: 'info',
            summary: 'Exporting',
            detail: 'Downloading PDF...',
        });

        let obs: Observable<Blob>;
        let filename = '';

        if (type === 'tb') {
            obs = this.periodService.exportTrialBalancePdf(id);
            filename = 'trial_balance.pdf';
        } else if (type === 'is') {
            obs = this.periodService.exportIncomeStatementPdf(id);
            filename = 'income_statement.pdf';
        } else {
            obs = this.periodService.exportCashFlowPdf(id);
            filename = 'cash_flow.pdf';
        }

        obs.subscribe((blob) => this.downloadFile(blob, filename));
    }

    exportExcel(type: 'tb' | 'is' | 'cf') {
        const id = this.selectedPeriodForReport.periodId;
        if (!id) return;

        this.messageService.add({
            severity: 'info',
            summary: 'Exporting',
            detail: 'Downloading Excel...',
        });

        let obs: Observable<Blob>;
        let filename = '';

        if (type === 'tb') {
            obs = this.periodService.exportTrialBalanceExcel(id);
            filename = 'trial_balance.xlsx';
        } else if (type === 'is') {
            obs = this.periodService.exportIncomeStatementExcel(id);
            filename = 'income_statement.xlsx';
        } else {
            obs = this.periodService.exportCashFlowExcel(id);
            filename = 'cash_flow.xlsx';
        }

        obs.subscribe((blob) => this.downloadFile(blob, filename));
    }

    getSeverity(status: PeriodStatus) {
        switch (status) {
            case PeriodStatus.OPEN:
                return 'success';
            case PeriodStatus.CLOSING:
                return 'warn';
            case PeriodStatus.CLOSED:
                return 'danger';
            default:
                return 'info';
        }
    }
}
