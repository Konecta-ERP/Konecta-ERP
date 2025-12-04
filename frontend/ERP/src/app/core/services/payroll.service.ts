import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class PayrollService {
    constructor(private _httpClient: HttpClient) {}

    getPayrollForEmployee(employeeId: number, yearMonth: string): Observable<any> {
        const params = new HttpParams().set('yearMonth', yearMonth);
        return this._httpClient.get(`${baseURL}/employees/${employeeId}/payroll`, { params });
    }
}
