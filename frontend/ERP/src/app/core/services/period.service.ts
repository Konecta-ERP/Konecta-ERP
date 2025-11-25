import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';
import { iCreatePeriod, iPeriod } from '../interfaces/iPeriod';

@Injectable({
    providedIn: 'root',
})
export class PeriodService {
    private controllerUrl = `${baseURL}/finance/periods`;
    private analyticsUrl = `${baseURL}/finance/analytics`;
    private pdfUrl = `${baseURL}/finance/pdf`;
    private excelUrl = `${baseURL}/finance/excel`;

    constructor(private _httpClient: HttpClient) {}

    // --- Period Management ---
    createPeriod(data: iCreatePeriod): Observable<any> {
        return this._httpClient.post(this.controllerUrl, data);
    }

    getAllPeriods(): Observable<any> {
        return this._httpClient.get(this.controllerUrl);
    }

    getLastSixPeriods(): Observable<any> {
        return this._httpClient.get(`${this.controllerUrl}/last`);
    }

    startClosingPeriod(id: number): Observable<any> {
        return this._httpClient.put(`${this.controllerUrl}/start-closing/${id}`, {});
    }

    lockPeriod(id: number): Observable<any> {
        return this._httpClient.put(`${this.controllerUrl}/lock/${id}`, {});
    }

    // --- Reports Data (View) ---
    getTrialBalanceReport(periodId: number): Observable<any> {
        return this._httpClient.get(`${this.analyticsUrl}/trial-balance/${periodId}`);
    }

    getIncomeStatement(periodId: number): Observable<any> {
        return this._httpClient.get(`${this.analyticsUrl}/income-statement/${periodId}`);
    }

    getCashFlowReport(periodId: number): Observable<any> {
        return this._httpClient.get(`${this.analyticsUrl}/cash-flow/${periodId}`);
    }

    // --- Export PDF ---
    exportTrialBalancePdf(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.pdfUrl}/trial-balance/${periodId}`, {
            responseType: 'blob',
        });
    }

    exportIncomeStatementPdf(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.pdfUrl}/income-statement/${periodId}`, {
            responseType: 'blob',
        });
    }

    exportCashFlowPdf(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.pdfUrl}/cash-flow/${periodId}`, {
            responseType: 'blob',
        });
    }

    // --- Export Excel ---
    exportTrialBalanceExcel(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.excelUrl}/trial-balance/${periodId}`, {
            responseType: 'blob',
        });
    }

    exportIncomeStatementExcel(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.excelUrl}/income-statement/${periodId}`, {
            responseType: 'blob',
        });
    }

    exportCashFlowExcel(periodId: number): Observable<Blob> {
        return this._httpClient.get(`${this.excelUrl}/cash-flow/${periodId}`, {
            responseType: 'blob',
        });
    }
}
