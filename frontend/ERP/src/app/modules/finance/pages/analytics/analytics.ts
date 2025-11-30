import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module'; // Ensure TabViewModule is exported here
import { MessageService } from 'primeng/api';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { iBalanceSheetReport, iGLResponse } from '../../../../core/interfaces/iAnalytics';
import { UserService } from '../../../../core/services/user.service';
import { isAdmin, isFinanceRole } from '../../../../core/constants/roles';

@Component({
    selector: 'app-analytics',
    standalone: true,
    imports: [SharedModule],
    providers: [MessageService],
    templateUrl: './analytics.html',
    styleUrls: ['./analytics.css'],
})
export class Analytics implements OnInit {
    // State
    activeTab: number = 0;
    loading: boolean = false;
    canView: boolean = false;

    // Balance Sheet Data
    bsDate: Date = new Date();
    bsReport: iBalanceSheetReport | null = null;

    // GL Data
    glStartDate: Date = new Date();
    glEndDate: Date = new Date();
    glReport: iGLResponse | null = null;

    constructor(
        private analyticsService: AnalyticsService,
        private userService: UserService,
        private messageService: MessageService
    ) {}

    ngOnInit() {
        this.checkPermissions();
        // Set defaults
        const today = new Date();
        this.glEndDate = today;
        this.glStartDate = new Date(today.getFullYear(), today.getMonth(), 1); // First day of month
    }

    checkPermissions() {
        const user = this.userService.getUser();
        this.canView = isFinanceRole(user?.role) || isAdmin(user?.role);
    }

    // --- Balance Sheet Logic ---
    loadBalanceSheet() {
        if (!this.bsDate) return;
        this.loading = true;
        const dateStr = this.formatDate(this.bsDate);

        this.analyticsService.getBalanceSheet(dateStr).subscribe({
            next: (res: any) => {
                this.bsReport = res.data;
                this.loading = false;
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load Balance Sheet',
                });
            },
        });
    }

    // --- General Ledger Logic ---
    loadGL() {
        if (!this.glStartDate || !this.glEndDate) return;
        this.loading = true;
        const startStr = this.formatDate(this.glStartDate);
        const endStr = this.formatDate(this.glEndDate);

        this.analyticsService.getGeneralLedger(startStr, endStr).subscribe({
            next: (res: any) => {
                this.glReport = res.data;
                this.loading = false;
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load General Ledger',
                });
            },
        });
    }

    // --- Export Logic ---
    downloadFile(data: Blob, filename: string) {
        const url = window.URL.createObjectURL(data);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        window.URL.revokeObjectURL(url);
    }

    exportBS(type: 'pdf' | 'excel') {
        if (!this.bsDate) return;
        const dateStr = this.formatDate(this.bsDate);
        const obs =
            type === 'pdf'
                ? this.analyticsService.exportBalanceSheetPdf(dateStr)
                : this.analyticsService.exportBalanceSheetExcel(dateStr);
        obs.subscribe((blob: Blob) =>
            this.downloadFile(blob, `balance_sheet_${dateStr}.${type === 'pdf' ? 'pdf' : 'xlsx'}`)
        );
    }

    exportGL(type: 'pdf' | 'excel') {
        if (!this.glStartDate || !this.glEndDate) return;
        const s = this.formatDate(this.glStartDate);
        const e = this.formatDate(this.glEndDate);
        const obs =
            type === 'pdf'
                ? this.analyticsService.exportGLPdf(s, e)
                : this.analyticsService.exportGLExcel(s, e);
        obs.subscribe((blob: Blob) =>
            this.downloadFile(
                blob,
                `general_ledger_${s}_to_${e}.${type === 'pdf' ? 'pdf' : 'xlsx'}`
            )
        );
    }

    formatDate(d: Date): string {
        return d.toISOString().split('T')[0];
    }
}
