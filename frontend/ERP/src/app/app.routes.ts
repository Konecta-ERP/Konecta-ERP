import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
    {
        path: 'auth',
        loadComponent: () => import('./layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
        children: [
            {
                path: 'login',
                loadComponent: () => import('./pages/login/login').then((m) => m.Login),
            },
            { path: '', redirectTo: 'login', pathMatch: 'full' },
        ],
    },
    {
        path: 'home',
        loadComponent: () => import('./layouts/home-layout/home-layout').then((m) => m.homeLayout),
        canActivate: [authGuard],
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
            },
            {
                path: 'profile',
                loadComponent: () => import('./pages/profile/profile').then((m) => m.Profile),
            },
            {
                path: 'profile/:id', loadComponent:()=>import('./pages/profile/profile').then(m=>m.Profile)
            },
            {
                path: 'finance',
                loadChildren: () =>
                    import('./modules/finance/finance.routes').then((m) => m.financeRoutes),
            },
            {
                path: 'hr',
                loadChildren: () => import('./modules/hr/hr.routes').then((m) => m.hrRoutes),
            },
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full',
            },
        ],
    },
    { path: '', redirectTo: 'auth', pathMatch: 'full' },
];
