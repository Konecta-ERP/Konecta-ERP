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
export class UserService {
  private userSubject = new BehaviorSubject<User | null>(null);
  user$ = this.userSubject.asObservable(); // for components that want to subscribe

  constructor(private _httpClient:HttpClient) {
  }

  setUser(user: User): void {
    this.userSubject.next({
        ...user,
        profilePictureUrl: user.profilePictureUrl || 'placeholderProfile.png'
    });
    }

    getUser(): User | null {
        return this.userSubject.value;
    }

    requestLeave(data:ILeaveRequestRequest):Observable<any> {
        return this._httpClient.post(`${baseURL}/leaves/request`,data);
    }

    login( data: ILogin):Observable<any>{
            return this._httpClient.post(`${baseURL}/identity/auth/login`,data);
    }
    logout():void{
        localStorage.removeItem('token');
        }

    authorized():boolean{
        return localStorage.getItem('token')!=null;
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
