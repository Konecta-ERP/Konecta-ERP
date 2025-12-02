import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class PayrollService {
    constructor(private _httpClient: HttpClient) {}

    getPayrollForEmployee(employeeId: number, yearMonth: string): Observable<any> {
        const body = { yearMonth };
        const req = new HttpRequest('GET', `${baseURL}/employees/${employeeId}/payroll`, body, {
            responseType: 'json',
        });
        return this._httpClient.request(req).pipe(map((event: any) => event.body || event));
    }
}
