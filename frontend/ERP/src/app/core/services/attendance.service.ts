import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class AttendanceService {
    constructor(private _httpClient: HttpClient) {}

    /**
     * Clock in for an employee
     */
    clockIn(employeeId: number): Observable<any> {
        return this._httpClient.post(`${baseURL}/employees/${employeeId}/clock-in`, {});
    }

    /**
     * Clock out for an employee
     */
    clockOut(employeeId: number): Observable<any> {
        return this._httpClient.post(`${baseURL}/employees/${employeeId}/clock-out`, {});
    }

    /**
     * Get latest attendance record to check if clocked in
     */
    getLatestAttendance(employeeId: number): Observable<any> {
        return this._httpClient.get(`${baseURL}/employees/${employeeId}/attendance/latest`);
    }
}
