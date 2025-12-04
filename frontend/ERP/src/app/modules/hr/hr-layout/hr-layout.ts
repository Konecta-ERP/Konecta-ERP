import { Component } from '@angular/core';
import { Sidebar } from './../../../components/sidebar/sidebar';
import { RouterOutlet } from '@angular/router';
@Component({
    selector: 'app-hr-layout',
    imports: [Sidebar, RouterOutlet],
    templateUrl: './hr-layout.html',
    styleUrl: './hr-layout.css',
})
export class HrLayout {
    userRole = 'hr';
    sidebarItems = [
        { label: 'Employees', icon: 'pi pi-users', route: 'employees' },
        { label: 'Recruitment', icon: 'pi pi-briefcase', route: 'recruitment' },
        { label: 'My Team', icon: 'pi pi-users', route: 'my-team' },
        { label: 'Payroll', icon: 'pi pi-wallet', route: 'payroll' },
    ];
}
