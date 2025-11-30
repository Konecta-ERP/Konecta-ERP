import { Routes } from '@angular/router';

export const hrRoutes: Routes = [
    {
        path: '',
        loadComponent: () => import('./hr-layout/hr-layout').then((m) => m.HrLayout),
        children: [
            {
                path: 'employees',
                loadComponent: () => import('./pages/employees/employees').then((m) => m.Employees),
            },
            {
                path: 'recruitment',
                loadComponent: () =>
                    import('./pages/recruitment/recruitment').then((m) => m.Recruitment),
            },
            {
                path: 'recruitment/job-post/:requisitionId',
                loadComponent: () =>
                    import('./pages/job-post-detail/job-post-detail').then((m) => m.JobPostDetail),
            },
            {
                path: 'my-team',
                loadComponent: () => import('./pages/my-team/my-team').then((m) => m.MyTeam),
            },
        ],
    },
];
