import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';

@Injectable({
    providedIn: 'root',
})
export class AnalyticsService {
    private analyticsUrl = `${baseURL}/finance/analytics`;
    private pdfUrl = `${baseURL}/finance/pdf`;
    private excelUrl = `${baseURL}/finance/excel`;
    private forecastUrl = `${baseURL}/finance/forecast`;

    constructor(private _httpClient: HttpClient) {}

    getBalanceSheet(asOfDate: string): Observable<any> {
        return this._httpClient.get(`${this.analyticsUrl}/balance-sheet/${asOfDate}`);
    }

    getGeneralLedger(fromDate: string, toDate: string, accountPKs?: number[]): Observable<any> {
        let params = new HttpParams().set('fromDate', fromDate).set('toDate', toDate);

        if (accountPKs && accountPKs.length > 0) {
            // Safely append multiple accountPKs using reduce
            params = accountPKs.reduce((p, id) => p.append('accountPKs', id.toString()), params);
        }

        return this._httpClient.get(`${this.analyticsUrl}/gl`, { params });
    }

    exportBalanceSheetPdf(asOfDate: string): Observable<Blob> {
        return this._httpClient.get(`${this.pdfUrl}/balance-sheet/${asOfDate}`, {
            responseType: 'blob',
        });
    }

    exportBalanceSheetExcel(asOfDate: string): Observable<Blob> {
        return this._httpClient.get(`${this.excelUrl}/balance-sheet/${asOfDate}`, {
            responseType: 'blob',
        });
    }

    exportGLPdf(fromDate: string, toDate: string, accountPKs?: number[]): Observable<Blob> {
        let params = new HttpParams().set('fromDate', fromDate).set('toDate', toDate);
        if (accountPKs && accountPKs.length > 0) {
            params = accountPKs.reduce((p, id) => p.append('accountPKs', id.toString()), params);
        }
        return this._httpClient.get(`${this.pdfUrl}/gl`, { params, responseType: 'blob' });
    }

    exportGLExcel(fromDate: string, toDate: string, accountPKs?: number[]): Observable<Blob> {
        let params = new HttpParams().set('fromDate', fromDate).set('toDate', toDate);
        if (accountPKs && accountPKs.length > 0) {
            params = accountPKs.reduce((p, id) => p.append('accountPKs', id.toString()), params);
        }
        return this._httpClient.get(`${this.excelUrl}/gl`, { params, responseType: 'blob' });
    }

    forecastRevenue(revT2: number, revT1: number) {
        return this._httpClient.post(this.forecastUrl, {
            revenueTwoQuartersAgo: revT2,
            revenueLastQuarter: revT1,
        });
    }
}
