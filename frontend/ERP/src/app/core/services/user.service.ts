import { Injectable } from '@angular/core';
import { User } from '../interfaces/iUser';
import { BehaviorSubject } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userSubject = new BehaviorSubject<User | null>(null);
  user$ = this.userSubject.asObservable(); // for components that want to subscribe

  constructor() {
    // Restore user from localStorage on refresh
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.userSubject.next(JSON.parse(storedUser));
    }
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




}
