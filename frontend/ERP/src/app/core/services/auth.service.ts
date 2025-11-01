import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { baseURL } from '../apiRoot/baseURL';
import { ILogin } from '../interfaces/ilogin';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor (private _httpClient:HttpClient){

  }

  login( data: ILogin):Observable<any>{
    return this._httpClient.post(`${baseURL}/auth`,data);
  }

  authorized():boolean{
    return localStorage.getItem('token')!=null;
  }
}
