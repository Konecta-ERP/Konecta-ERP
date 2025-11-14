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

    getDepartments(): Observable<IDepartment[]> {
        const currentDepartments = this.departmentsSubject.value;
        if (currentDepartments && currentDepartments.length > 0) {
            return of(currentDepartments);
        } else {
            return this.loadDepartmentsFromApi();
        }
    }

    loadDepartmentsFromApi(): Observable<IDepartment[]> {
        return this._httpClient.get<IDepartment[]>(`${baseURL}/departments`).pipe(
            tap((departments) => this.departmentsSubject.next(departments))
        );
    }
}
