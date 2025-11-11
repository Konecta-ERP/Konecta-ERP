import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { HttpClient } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { ILeaveRequestRequest } from '../interfaces/iLeaveRequestRequest';
import { Observable } from 'rxjs';
import { UserService } from './user.service';
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

}
