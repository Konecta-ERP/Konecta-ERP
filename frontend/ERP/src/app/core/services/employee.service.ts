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

    //leave request API calls
    requestLeave(data: ILeaveRequestRequest): Observable<any> {
        const user = this._userService.getUser();


        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }

        const employeeId = user.employeeId;
        console.log("Requesting leave for employeeId:", employeeId, "with data:", data);
        return this._httpClient.post(`${baseURL}/employees/${employeeId}/leave-requests`, data);
    }

    updateLeaveRequest(leaveId:string, data: any): Observable<any> {
        return this._httpClient.patch(`${baseURL}/leave-request/${leaveId}`, data);
    }


    getLeaveRequests():Observable<any>{
        const user = this._userService.getUser();
        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }
        const employeeId = user.employeeId;

        return this._httpClient.get(`${baseURL}/employees/${employeeId}/leave-requests`);
    }

    getLeaveRequestPerDepartment(id:Number):Observable<any>{
        return this._httpClient.get(`${baseURL}/departments/${id}/leave-requests/next-month`);
    }

    deleteLeaveRequest(id:string):Observable<any>{
        return this._httpClient.delete(`${baseURL}/leave-requests/${id}`);
    }

    getLeaveBalance():Observable<any>{
        const user = this._userService.getUser();
        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }
        const employeeId = user.id;

        return this._httpClient.get(`${baseURL}/employees/${employeeId}/leave-balance`);
    }

    acceptleaveRequest(leaveId:string):Observable<any>{
        const body = {
            status:"APPROVED"
        };
        return this._httpClient.patch(`${baseURL}/leave-requests/${leaveId}/status`, body);
    }

    rejectleaveRequest(leaveId:string):Observable<any>{
        const body = {
            status:"REJECTED"
        };
        return this._httpClient.patch(`${baseURL}/leave-requests/${leaveId}/status`, body);
    }


    // get employee by user ID
    getEmployeeByUserId(userId:string):Observable<any>{
        console.log("Fetching employee details for userId:", userId);
        return this._httpClient.get(`${baseURL}/employees/by-user/${userId}`);
    }

    // performance reviews apis
    getPerformanceReviews():Observable<any>{
        return this._httpClient.get(`${baseURL}/performance-reviews/employee/reviews`);
    }

    // employee goals APIs
    getEmployeeGoals():Observable<any>{
        const user = this._userService.getUser();
        if (!user || !user.id) {
            throw new Error('User not logged in or missing ID');
        }
        const employeeId = user.employeeId;
        return this._httpClient.get(`${baseURL}/employees/${employeeId}/goals`);
    }

    //search employees api
    searchEmployees(filters: IEmployeeSearchFilter): Observable<any> {
        const params = new URLSearchParams();

        if (filters.name) params.set('name', filters.name);
        if (filters.department) params.set('department', filters.department);
        if (filters.position) params.set('position', filters.position);

        const url = `${baseURL}/employees/search?${params.toString()}`;

        return this._httpClient.get(url);
    }


    getEmployeeById(id:string):Observable<any>{
        return this._httpClient.get<User>(`${baseURL}/employees/${id}`);
    }

    getDepartmentEmployees(departmentId: number): Observable<any> {
        return this._httpClient.get(`${baseURL}/departments/${departmentId}/employees`);
    }

}
