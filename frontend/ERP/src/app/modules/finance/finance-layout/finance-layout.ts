import { Component } from '@angular/core';
import { Sidebar } from './../../../components/sidebar/sidebar';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-finance-layout',
    imports: [Sidebar, RouterOutlet],
    templateUrl: './finance-layout.html',
    styleUrl: './finance-layout.css',
})
export class FinanceLayout {
    userRole = 'finance';
    sidebarItems = [
        { label: 'Accounts', icon: 'pi pi-wallet', route: 'accounts' },
        { label: 'Transactions', icon: 'pi pi-credit-card', route: 'transactions' },
        { label: 'Periods', icon: 'pi pi-calendar-clock', route: 'periods' },
        { label: 'Analytics', icon: 'pi pi-chart-bar', route: 'analytics' },
    ];
}
