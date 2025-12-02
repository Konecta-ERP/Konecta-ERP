import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { EmployeeService } from '../../../../core/services/employee.service';
import { PayrollService } from '../../../../core/services/payroll.service';
import { DepartmentService } from '../../../../core/services/department.service';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { IEmployeeSearchFilter } from '../../../../core/interfaces/iEmployeeSearchFilter';
import { IDepartment } from '../../../../core/interfaces/iDepartment';
import { User } from '../../../../core/interfaces/iUser';
import { IPayrollSummary } from '../../../../core/interfaces/iPayrollSummary';

@Component({
    selector: 'app-payroll',
    imports: [SharedModule],
    templateUrl: './payroll.html',
    styleUrl: './payroll.css',
})
export class Payroll implements OnInit {
    filters: IEmployeeSearchFilter = {};
    employeeList: User[] = [];
    selectedEmployee: User | null = null;
    departments: IDepartment[] = [];
    dropDownDepartments: { label: string; value: string }[] = [];

    yearMonth: string = '';
    payrollData: IPayrollSummary | null = null;
    showPayrollResults = false;

    constructor(
        private _employeeService: EmployeeService,
        private _payrollService: PayrollService,
        private _departmentService: DepartmentService,
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService
    ) {}

    ngOnInit(): void {
        this.loadDepartments();
        this.setDefaultYearMonth();
    }

    setDefaultYearMonth(): void {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        this.yearMonth = `${year}-${month}`;
    }

    onSearchEmployees(): void {
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
                    if (this.employeeList.length === 0) {
                        this.show(
                            'info',
                            'No Results',
                            'No employees found matching your search criteria'
                        );
                    }
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employees');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show(
                    'error',
                    'Error',
                    err?.error?.cMessage || 'An error occurred while loading employees'
                );
            },
        });
    }

    onSelectEmployee(employee: User): void {
        this.selectedEmployee = employee;
        this.employeeList = [];
        this.filters = {};
        this.payrollData = null;
        this.showPayrollResults = false;
    }

    onClearEmployeeSelection(): void {
        this.selectedEmployee = null;
        this.payrollData = null;
        this.showPayrollResults = false;
    }

    onClearFilters(): void {
        this.filters = {
            name: '',
            position: '',
            department: '',
        };
        this.employeeList = [];
    }

    onGetPayroll(): void {
        if (!this.selectedEmployee || !this.selectedEmployee.employeeId) {
            this.show('warn', 'Warning', 'Please select an employee first');
            return;
        }

        if (!this.yearMonth) {
            this.show('warn', 'Warning', 'Please select a year and month');
            return;
        }

        this._NgxSpinnerService.show();
        this._payrollService
            .getPayrollForEmployee(Number(this.selectedEmployee.employeeId), this.yearMonth)
            .subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        this.payrollData = res.data;
                        this.showPayrollResults = true;
                        this.show('success', 'Success', 'Payroll details retrieved successfully');
                    } else {
                        this.show(
                            'error',
                            'Error',
                            res.cMessage || 'Failed to load payroll details'
                        );
                    }
                },
                error: (err) => {
                    this._NgxSpinnerService.hide();
                    this.show(
                        'error',
                        'Error',
                        err?.error?.cMessage || 'An error occurred while loading payroll details'
                    );
                },
            });
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(amount);
    }

    getDetailsByType(type: string): any[] {
        if (!this.payrollData?.details) return [];
        return this.payrollData.details.filter((d) => d.type === type);
    }

    show(
        severity: string = 'info',
        summary: string = 'Info',
        detail: string = 'Message Content'
    ): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

    loadDepartments(): void {
        if (this._departmentService.hasCachedDepartments()) {
            this.departments = this._departmentService.getCachedDepartments();
            this.dropDownDepartments = this.departments.map((dept) => ({
                label: dept.name,
                value: dept.name,
            }));
        } else {
            this._departmentService.getDepartments().subscribe({
                next: (res) => {
                    if (res.status === 200 && res.data) {
                        this._departmentService.setDepartments(res.data);
                        this.departments = this._departmentService.getCachedDepartments();
                        this.dropDownDepartments = this.departments.map((dept) => ({
                            label: dept.name,
                            value: dept.name,
                        }));
                    } else {
                        this.show('error', 'Error', res.cMessage || 'Failed to load departments');
                    }
                },
                error: (err) => {
                    this.show(
                        'error',
                        'Error',
                        err?.error?.cMessage || 'An error occurred while loading departments'
                    );
                },
            });
        }
    }
}
