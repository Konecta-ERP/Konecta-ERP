import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { IEmployeeGoalResponse } from '../../core/interfaces/iEmployeeGoalResponse';
import { NgxSpinnerService } from 'ngx-spinner';
import { EmployeeService } from '../../core/services/employee.service';
@Component({
    selector: 'app-employee-goals',
    imports: [SharedModule],
    templateUrl: './employee-goals.html',
    styleUrl: './employee-goals.css',
    providers: [MessageService]
})
export class EmployeeGoals implements OnInit {

    ngOnInit(): void {
        this.loadEmployeeGoals();
    }
    constructor(
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _employeeService: EmployeeService
    ){}


    goalDialogVisible = false;
    selectedGoal: IEmployeeGoalResponse | null = null;

    viewGoal(goal: IEmployeeGoalResponse) {
    this.selectedGoal = goal;
    this.goalDialogVisible = true;
    }
    employeeGoals:IEmployeeGoalResponse[]=[]

    loadEmployeeGoals(): void {
        console.log('Loading employee goals...');
        this._NgxSpinnerService.show();
        this._employeeService.getEmployeeGoals().subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {
                    this.employeeGoals = res.data;
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employee goals');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading employee goals');
            }
        });
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

}
