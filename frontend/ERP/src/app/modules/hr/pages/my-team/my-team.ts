import { Component, OnInit } from '@angular/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import { FullCalendarPlugins } from './../../../../shared/functions/fullcalendar-plugins';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { UserService } from '../../../../core/services/user.service';
import { EmployeeService } from '../../../../core/services/employee.service';
import { ILeaveRequestResponse } from '../../../../core/interfaces/iLeaveRequestResponse';
@Component({
  selector: 'app-my-team',
  imports: [FullCalendarModule, SharedModule],
  templateUrl: './my-team.html',
  styleUrl: './my-team.css',
})
export class MyTeam implements OnInit {

    constructor(
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _userService: UserService,
        private _employeeService: EmployeeService
    ) {
        this.loadLeaveRequests();
    }
    leaveRequests: ILeaveRequestResponse[] = [];
    leaveCounts: Record<string, number> = {};

    ngOnInit(): void {
        this.loadLeaveRequests();
        this.leaveCounts = this.leaveRequests.reduce((acc: Record<string, number>, request) => {
            for (let d = new Date(request.startDate); d <= new Date(request.endDate); d.setDate(d.getDate() + 1)) {
                const dateStr = d.toISOString().split('T')[0];
                acc[dateStr] = (acc[dateStr] || 0) + 1;
            }
            return acc;
        }, {});

    }

    calendarOptions = {
        plugins: FullCalendarPlugins,
        initialView: 'dayGridMonth',

        selectable: true,
        editable: false,

        dateClick: (info: any) => {
            alert("You clicked: " + info.dateStr);
        },

        dayCellContent: (arg: any) => {
            const date = arg.date.toISOString().split('T')[0];
            const count = this.leaveCounts[date] || 0;

            return {
            html: `
                <div class="flex flex-col items-center">
                <div>${arg.dayNumberText}</div>
                <div class="text-xs text-blue-600">
                    ${count > 0 ? count + ' on leave' : ''}
                </div>
                </div>
            `
            };
        }
    };


    loadLeaveRequests() {

        const user = this._userService.getUser();
        console.log("Current User:", user);
        if (!user||!user.departmentId) {
        console.error("User has no department_id");
        return;
        }
        this._NgxSpinnerService.show();
        this._employeeService.getLeaveRequestPerDepartment(user.departmentId).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {

                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load leave requests');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading leave requests');
            }
        });
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

}
