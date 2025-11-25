import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { EmployeeGoals } from '../../components/employee-goals/employee-goals';
import { EmployeeLeaveRequests } from '../../components/employee-leave-requests/employee-leave-requests';
import { EmployeePerformanceReviews } from '../../components/employee-performance-reviews/employee-performance-reviews';
import { UserService } from '../../core/services/user.service';
import { EmployeeService } from '../../core/services/employee.service';
import { DepartmentService } from '../../core/services/department.service';
import { User } from '../../core/interfaces/iUser';
import { NgxSpinnerService } from 'ngx-spinner';
import { MessageService } from 'primeng/api';
interface CardConfig {
  title: string;
  subtitle: string;
  imageUrl: string;
  route?: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [SharedModule, EmployeeGoals,EmployeeLeaveRequests,EmployeePerformanceReviews],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
    constructor(
        private _userService: UserService,
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _employeeService: EmployeeService,
        private _departmentService: DepartmentService
    ){}

    ngOnInit(): void {
        /* this.loadDepartments();
        this.loadEmployeeDetails(); */
    }

    activeTab = 'Career Goals';

    setActive(tab: string) {
        this.activeTab = tab;
    }
    /*loadDepartments(): void {
        this._departmentService.getDepartments().subscribe({
            next: (res) => {
                if (res.status === 200 && res.data) {
                    this._departmentService.setDepartments(res.data);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load departments');
                }
            },
            error: (err) => {
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading departments');
            }
        });
    }

    loadEmployeeDetails(): void {
        const user = this._userService.getUser();
        if (!user || !user.id) {
            console.error("User has no user ID");
            return;
        }

        this._employeeService.getEmployeeByUserId(user.id).subscribe({
            next: (res) => {
                if (res.status === 200) {
                    user.employeeId = res.data.employeeId;
                    user.departmentName= res.data.departmentName;
                    user.position= res.data.positionTitle;
                    user.departmentId = Number(this._departmentService.getDepartmentIdByName(res.data.departmentName))
                    user.salaryNet = res.data.salaryNet;
                    user.salaryGross = res.data.salaryGross;
                    this._userService.setUser(user);
                    console.log("Updated user with employee details:", user);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employee details');
                }
            },
            error: (err) => {
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading employee details');
            }
        });
    } */


    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
