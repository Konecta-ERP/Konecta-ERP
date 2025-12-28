import { Component } from '@angular/core';
import { Sidebar } from './../../../components/sidebar/sidebar';
import { RouterOutlet } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
@Component({
    selector: 'app-hr-layout',
    imports: [Sidebar, RouterOutlet],
    templateUrl: './hr-layout.html',
    styleUrl: './hr-layout.css',
})
export class HrLayout {
    constructor(private _userService:UserService) {
        this.addroles();
    }
    userRole = 'hr';
    sidebarItems = [
        { label: 'Payroll', icon: 'pi pi-wallet', route: 'payroll' },
    ];

    addroles(){
        const user = this._userService.getUser();
        if(user)
        {
            console.log(user.role);
            if(user.role == 'ADMIN'|| user.role == 'HR_MANAGER')
            {
                this.sidebarItems.unshift(
                    { label: 'Employees', icon: 'pi pi-users', route: 'employees' },
                    { label: 'Recruitment', icon: 'pi pi-briefcase', route: 'recruitment' },
                    { label: 'My Team', icon: 'pi pi-users', route: 'my-team' },
                );
            }
            if(user.role == 'MANAGER')
            {
                this.sidebarItems.unshift(
                    { label: 'My Team', icon: 'pi pi-users', route: 'my-team' },
                    { label: 'Recruitment', icon: 'pi pi-briefcase', route: 'recruitment' },
                );
            }

            if(user.role == 'HR_ASSOCIATE')
            {
                this.sidebarItems.unshift(
                    { label: 'Recruitment', icon: 'pi pi-briefcase', route: 'recruitment' })
            }
        }
    }
}
