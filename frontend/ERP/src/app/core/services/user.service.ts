import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { baseURL } from '../apiRoot/baseURL';
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

    login( data: ILogin):Observable<any>{
            return this._httpClient.post(`${baseURL}/identity/auth/login`,data);
    }
    logout():void{
        localStorage.removeItem('token');
        }

    authorized():boolean{
        return localStorage.getItem('token')!=null;
    }



}
