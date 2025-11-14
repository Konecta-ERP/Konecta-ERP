import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { ActivatedRoute, Router } from '@angular/router';
import { IEmployeeSearchFilter } from '../../../../core/interfaces/iEmployeeSearchFilter';
import { EmployeeService } from '../../../../core/services/employee.service';
import { DepartmentService } from '../../../../core/services/department.service';
import { IDepartment } from '../../../../core/interfaces/iDepartment';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { iEmployeeSearchResponse } from '../../../../core/interfaces/iEmployeeSearchResponse';
import { EmployeeCard } from '../../../../components/employee.card/employee.card';

@Component({
  selector: 'app-employees',
  imports: [SharedModule, EmployeeCard],
  templateUrl: './employees.html',
  styleUrl: './employees.css',
})
export class Employees implements OnInit {

    filters: IEmployeeSearchFilter = {};
    employeeList:iEmployeeSearchResponse[] = [];

    departments: IDepartment[] = [];

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _employeeService: EmployeeService,
        private _departmentService: DepartmentService
    ) {}

    ngOnInit(): void {
        // ✅ Restore filters from query params
        this.route.queryParams.subscribe((params) => {
        this.filters = {
            name: params['name'],
            department: params['department'],
            position: params['position']
        };

        // Only search if there are any filters in the URL
        if (Object.keys(params).length > 0) {
            this.searchEmployees();
        }
        });

        // ✅ Load departments for dropdown
        this._departmentService.getDepartments().subscribe((departments) => {
            this.departments = departments;
        });
    }

    onSearch(): void {
        const queryParams: Record<string, string> = {};
        Object.entries(this.filters).forEach(([key, value]) => {
        if (value && value.trim() !== '') {
            queryParams[key] = value.trim();
        }
        });

        this.router.navigate([], {
        queryParams,
        queryParamsHandling: 'merge'
        });

        this.searchEmployees();
    }

    private searchEmployees(): void {
        const cleanedFilters: IEmployeeSearchFilter = {};
        Object.entries(this.filters).forEach(([key, value]) => {
        if (value && value.trim() !== '') {
            cleanedFilters[key as keyof IEmployeeSearchFilter] = value.trim();
        }
        });
        this._NgxSpinnerService.show();
        this._employeeService.searchEmployees(cleanedFilters).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {
                    this.employeeList = res.data;
                }
                else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employees');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading employees');
            }
        });
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
