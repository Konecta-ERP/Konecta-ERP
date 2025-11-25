import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { DepartmentService } from '../services/department.service';
import { EmployeeService } from '../services/employee.service';
import { forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
export const authGuard: CanActivateFn = (route, state) => {
    const userService = inject(UserService);
    const departmentService = inject(DepartmentService);
    const employeeService = inject(EmployeeService);
    const router = inject(Router);
    const token = localStorage.getItem('token');

    if (!token) {
        return router.navigate(['/auth']).then(() => false);
    }

    const user = userService.getUser();

    console.log("AuthGuard: Current user:", user);

    const departmentsLoaded = departmentService.hasCachedDepartments()
    const employeeLoaded = user?.employeeId;

    if (departmentsLoaded && employeeLoaded) {
        return true;
    }

    // Load required data before allowing navigation
    const departments$ = departmentsLoaded
        ? of(null)
        : departmentService.getDepartments().pipe(
            map(res => {
            if (res.status === 200 && res.data) {
                departmentService.setDepartments(res.data);
            }
            return null;
            }),
            catchError(() => of(null))
        );

    const employee$ = (employeeLoaded || !user?.id)
        ? of(null)
        : employeeService.getEmployeeByUserId(user.id).pipe(
            map(res => {
            if (res.status === 200) {
                user.employeeId = res.data.employeeId;
                user.departmentName = res.data.departmentName;
                user.position = res.data.positionTitle;
                user.departmentId = Number(departmentService.getDepartmentIdByName(res.data.departmentName));
                user.salaryNet = res.data.salaryNet;
                user.salaryGross = res.data.salaryGross;
                userService.setUser(user);
            }
            return null;
            }),
            catchError(() => of(null))
        );

    // Wait for both requests to complete before allowing navigation
    return forkJoin([departments$, employee$]).pipe(
        map(() => true),
        catchError(() => of(router.createUrlTree(['/login'])))
    );
};
