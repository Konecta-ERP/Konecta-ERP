import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { BehaviorSubject } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
import { Observable } from 'rxjs';
import { ILogin } from '../interfaces/ilogin';
@Injectable({
    providedIn: 'root',
})
export class UserService {
    private userSubject = new BehaviorSubject<User | null>(null);
    user$ = this.userSubject.asObservable(); // for components that want to subscribe

    constructor(private _httpClient: HttpClient) {}

    setUser(user: User): void {
        this.userSubject.next({
            ...user,
            profilePictureUrl: user.profilePictureUrl || 'placeholderProfile.png',
        });
        localStorage.setItem('user', JSON.stringify(this.userSubject.value));
    }

    getUser(): User | null {
        if (!this.userSubject.value) {
            const userData = localStorage.getItem('user');
            if (userData) {
                this.userSubject.next(JSON.parse(userData));
            }
        }
        return this.userSubject.value;
    }

    login(data: ILogin): Observable<any> {
        return this._httpClient.post(`${baseURL}/identity/auth/login`, data);
    }

    forgotPassword(data: { email: string }): Observable<any> {
        return this._httpClient.post(`${baseURL}/identity/forgot-password`, data);
    }

    verifyOTP(data: { email: string; otp: string }): Observable<any> {
        return this._httpClient.post(`${baseURL}/identity/verify-otp`, data);
    }

    resetPassword(data: { newPassword: string; confirmPassword: string }): Observable<any> {
        const resetToken = localStorage.getItem('resetToken');
        let headers = new HttpHeaders();
        if (resetToken) {
            headers = headers.set('Authorization', `Bearer ${resetToken}`);
        }
        return this._httpClient.post(`${baseURL}/identity/reset-password`, data, { headers });
    }

    logout(): Boolean {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        return true;
    }

    authorized(): boolean {
        return localStorage.getItem('token') != null;
    }

    fromFinanceDepartment(): boolean {
        const user = this.getUser();
        return user?.role === 'CFO'|| user?.role === 'ACCOUNTANT';
    }

    fromHRDepartment(): boolean {
        const user = this.getUser();
        return user?.role === 'HR';
    }
}
