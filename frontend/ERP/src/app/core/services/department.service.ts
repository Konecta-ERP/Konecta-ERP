import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IDepartment } from './../interfaces/iDepartment'; // optional if you keep the interface in a separate file
import { baseURL } from '../apiRoot/baseURL';

@Injectable({
  providedIn: 'root',
})
export class DepartmentService {
    private departmentsSubject = new BehaviorSubject<IDepartment[]>([]);
    departments$ = this.departmentsSubject.asObservable();

    constructor(private _httpClient: HttpClient) {}

    // Get departments (returns cached if available)
    getDepartments(): Observable<any> {
        return this._httpClient.get(`${baseURL}/departments`);
    }

    // Save departments to cache
    setDepartments(departments: IDepartment[]): void {
        this.departmentsSubject.next(departments);
        console.log('Departments cached:', this.departmentsSubject.value);
    }

    // Get cached departments
    getCachedDepartments(): IDepartment[] {
        console.log('Retrieving cached departments:', this.departmentsSubject.value);
        return this.departmentsSubject.value;
    }

    // Check if departments are cached
    hasCachedDepartments(): boolean {
        return this.departmentsSubject.value.length > 0;
    }

    // Clear cache
    clearCache(): void {
        this.departmentsSubject.next([]);
    }
}
