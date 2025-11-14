import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { HttpClient } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { ILeaveRequestRequest } from '../interfaces/iLeaveRequestRequest';
import { Observable } from 'rxjs';
import { UserService } from './user.service';
import { IEmployeeSearchFilter } from '../interfaces/iEmployeeSearchFilter';
@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

    constructor(private _httpClient:HttpClient, private _userService: UserService) {}

    requestLeave(data: ILeaveRequestRequest): Observable<any> {
        const user = this._userService.getUser();


        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }

        const employeeId = user.id;
        return this._httpClient.post(`${baseURL}/employees/${employeeId}/leave-requests`, data);
    }


    getLeaveRequests():Observable<any>{
        const user = this._userService.getUser();
        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }
        const employeeId = user.id;

        return this._httpClient.get(`${baseURL}/employees/${employeeId}/leave-requests`);
    }

    deleteLeaveRequest(id:string):Observable<any>{
        return this._httpClient.delete(`${baseURL}/leaves/request/${id}`);
    }

    getPerformanceReviews():Observable<any>{
        return this._httpClient.get(`${baseURL}/performance-reviews/employee/reviews`);
    }

    getEmployeeGoals():Observable<any>{
        return this._httpClient.get(`${baseURL}/employee-goals/employee/goals`);
    }

    searchEmployees(filters: IEmployeeSearchFilter): Observable<any> {
        const params = new URLSearchParams();

        if (filters.name) params.set('name', filters.name);
        if (filters.department) params.set('department', filters.department);
        if (filters.position) params.set('position', filters.position);

        const url = `${baseURL}/employees/search?${params.toString()}`;

        return this._httpClient.get(url);
    }

}
