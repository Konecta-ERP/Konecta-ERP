import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { ActivatedRoute, Router } from '@angular/router';
import { IEmployeeSearchFilter } from '../../../../core/interfaces/iEmployeeSearchFilter';
import { EmployeeService } from '../../../../core/services/employee.service';
import { DepartmentService } from '../../../../core/services/department.service';
import { IDepartment } from '../../../../core/interfaces/iDepartment';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { EmployeeCard } from '../../../../components/employee.card/employee.card';
import { User } from '../../../../core/interfaces/iUser';

@Component({
  selector: 'app-employees',
  imports: [SharedModule, EmployeeCard],
  templateUrl: './employees.html',
  styleUrl: './employees.css',
})
export class Employees implements OnInit {

    filters: IEmployeeSearchFilter = {};
    employeeList:User[] = [];
    isLoading = false;
    pageSize = 9;
    totalRecords = 0;
    viewMode: 'grid' | 'list' = 'grid';
    departments: IDepartment[] = [];
    dropDownDepartments: { label: string; value: string }[] = [];
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
        if (this._departmentService.hasCachedDepartments()) {
            this.departments = this._departmentService.getCachedDepartments();
            this.dropDownDepartments = this.departments.map(dept => ({
                label: dept.name,
                value: dept.name
            }));
            console.log('Loaded departments from cache:', this.departments);
        } else {
            // Load from API if no cache
            this.loadDepartments();
        }
    }

    onSearch(): void {
        const queryParams: Record<string, string> = {};
        Object.entries(this.filters).forEach(([key, value]) => {
            console.log('Filter value:', key, value);
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
    onClearFilters(): void {
        this.filters = {
        name: '',
        position: '',
        department: ''
        };
        this.employeeList = [];
    }

  onPageChange(event: any): void {
    // Handle pagination
    console.log('Page changed:', event);
  }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

    loadDepartments(): void {
        this._departmentService.getDepartments().subscribe({
            next: (res) => {
                if (res.status === 200 && res.data) {
                    this._departmentService.setDepartments(res.data);

                    this.departments = this._departmentService.getCachedDepartments();
                    this.dropDownDepartments = this.departments.map(dept => ({
                        label: dept.name,
                        value: dept.name
                    }));
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load departments');
                }
            },
            error: (err) => {
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading departments');
            }
        });
    }
}
