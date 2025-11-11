import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { ILeaveRequestRequest } from '../interfaces/iLeaveRequestRequest';
import { Observable } from 'rxjs';
import { ILogin } from '../interfaces/ilogin';
@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

    constructor(private _httpClient:HttpClient) {}

    requestLeave(data:ILeaveRequestRequest):Observable<any> {
        return this._httpClient.post(`${baseURL}/leaves/request`,data);
    }

    getLeaveRequests():Observable<any>{
        return this._httpClient.get(`${baseURL}/leaves/employee/requests`);
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
