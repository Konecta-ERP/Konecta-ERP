import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { MenuItem } from 'primeng/api';
import { RouterLink } from "@angular/router";
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/interfaces/iUser';
@Component({
  selector: 'app-home-nav',
  imports: [SharedModule, RouterLink],
  templateUrl: './home-nav.html',
  styleUrl: './home-nav.css',
})
export class HomeNav {

    constructor(private _userService: UserService, ){}
    items: MenuItem[] | undefined;
    userMenuItems: MenuItem[] = [];
    ngOnInit() {
        this.items = [
            {
                label: 'Dashboard',
                path: 'dashboard'
            }
        ];

        this.userMenuItems = [
            {
                label: 'Profile',
                icon: 'pi pi-user',
                routerLink: 'profile',
            },
            {
                label: 'Logout',
                icon: 'pi pi-sign-out',
                command: () => this.logout(),
            },
        ];

        if (this._userService.fromFinanceDepartment()||true) {
            this.items.push({
                label: 'Finance',
                path: 'finance'
            });
        }
        if (this._userService.fromHRDepartment()||true) {
            this.items.push({
                label: 'HR',
                path: 'hr'
            });
        }


    }



    logout() {
        if(this._userService.logout()){
            window.location.reload();
        }
    }
}
