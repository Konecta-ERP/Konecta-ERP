import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { iBalanceSheetReport, iGLResponse } from '../../../../core/interfaces/iAnalytics';
import { UserService } from '../../../../core/services/user.service';
import { isAdmin, isFinanceRole } from '../../../../core/constants/roles';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TabsModule } from 'primeng/tabs';

import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { PanelModule } from 'primeng/panel';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { ChartModule } from 'primeng/chart';
import { ToastModule } from 'primeng/toast';

@Component({
    selector: 'app-analytics',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TabsModule, // <--- The critical fix
        CardModule,
        ButtonModule,
        TableModule,
        TagModule,
        PanelModule,
        DatePickerModule,
        InputNumberModule,
        ChartModule,
        ToastModule,
    ],
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

    // Forecast Data
    revenueT2: number | null = null; // Revenue 2 Quarters Ago
    revenueT1: number | null = null; // Revenue Last Quarter
    prediction: number | null = null;

    // Chart Config
    chartData: any;
    chartOptions: any;

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
        this.initChartOptions();
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

    // --- NEW: Forecast Logic ---
    runForecast() {
        if (!this.revenueT2 || !this.revenueT1) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Missing Input',
                detail: 'Please enter both revenue figures.',
            });
            return;
        }

        this.loading = true;
        this.analyticsService.forecastRevenue(this.revenueT2, this.revenueT1).subscribe({
            next: (res: any) => {
                this.prediction = res.data.predictedNextQuarterRevenue;
                this.updateChart();
                this.loading = false;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Forecast Ready',
                    detail: 'Revenue predicted successfully',
                });
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Prediction failed.',
                });
            },
        });
    }

    updateChart() {
        if (!this.revenueT2 || !this.revenueT1 || !this.prediction) return;

        this.chartData = {
            labels: ['Q-2 (Historical)', 'Q-1 (Historical)', 'Next Q (Forecast)'],
            datasets: [
                {
                    label: 'Revenue Trend',
                    data: [this.revenueT2, this.revenueT1, this.prediction],
                    fill: false,
                    borderColor: '#4F46E5', // Indigo
                    tension: 0.4,
                    pointBackgroundColor: ['#6B7280', '#6B7280', '#10B981'], // Grey for history, Green for prediction
                    pointRadius: 6,
                    borderDash: [0, 0], // Solid line
                },
                {
                    label: 'Projection',
                    data: [null, this.revenueT1, this.prediction], // Overlay dotted line for the forecast part
                    fill: false,
                    borderColor: '#10B981', // Green
                    borderDash: [5, 5],
                    tension: 0.4,
                },
            ],
        };
    }

    initChartOptions() {
        const documentStyle = getComputedStyle(document.documentElement);
        const textColor = documentStyle.getPropertyValue('--text-color');
        const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
        const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

        this.chartOptions = {
            maintainAspectRatio: false,
            responsive: true,
            layout: {
                padding: 20,
            },
            plugins: {
                legend: {
                    labels: { color: textColor },
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                },
            },
            scales: {
                x: {
                    ticks: { color: textColorSecondary },
                    grid: { color: surfaceBorder, drawBorder: false },
                },
                y: {
                    beginAtZero: false, // Lets the chart zoom in on the relevant range
                    ticks: {
                        color: textColorSecondary,
                        callback: (value: any) => {
                            return '$' + (value / 1000000).toFixed(1) + 'M';
                        },
                    },
                    grid: { color: surfaceBorder, drawBorder: false },
                },
            },
        };
    }

    formatDate(d: Date): string {
        return d.toISOString().split('T')[0];
    }
}
