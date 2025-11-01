import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
    {path: 'auth', loadComponent:()=>import('./layouts/auth-layout/auth-layout').then(m=>m.AuthLayout), children: [
        {path: 'login', loadComponent:()=>import('./pages/login/login').then(m=>m.Login)},
        {path: '', redirectTo: 'login', pathMatch: 'full' }
    ]},
    {
        path: 'user', loadComponent:()=>import('./layouts/user-layout/user-layout').then(
            m=>m.UserLayout
        ),
        canActivate:[authGuard]
    },
    {path: '', redirectTo: 'auth', pathMatch: 'full' },
];
