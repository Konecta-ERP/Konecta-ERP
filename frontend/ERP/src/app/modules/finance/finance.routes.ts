import { Routes } from '@angular/router';

export const financeRoutes: Routes = [
    {
        path: '',
        loadComponent: () => import('./finance-layout/finance-layout').then((m) => m.FinanceLayout),
        children: [
            {
                path: 'accounts',
                loadComponent: () => import('./pages/accounts/accounts').then((m) => m.Accounts),
            },
            {
                path: 'transactions',
                loadComponent: () =>
                    import('./pages/transactions/transactions').then((m) => m.Transactions),
            },
            {
                path: 'periods',
                loadComponent: () => import('./pages/periods/periods').then((m) => m.Periods),
            },
            {
                path: 'analytics',
                loadComponent: () => import('./pages/analytics/analytics').then((m) => m.Analytics),
            },
        ],
    },
];
