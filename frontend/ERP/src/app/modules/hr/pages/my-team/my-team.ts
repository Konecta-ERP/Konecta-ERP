import { Component, OnInit, ViewChild } from '@angular/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import { FullCalendarPlugins } from './../../../../shared/functions/fullcalendar-plugins';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { UserService } from '../../../../core/services/user.service';
import { EmployeeService } from '../../../../core/services/employee.service';
import { ILeaveRequestResponse } from '../../../../core/interfaces/iLeaveRequestResponse';
import { IEmployeesLeaves } from '../../../../core/interfaces/iEmployeesLeaves';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { EmployeeCard } from '../../../../components/employee.card/employee.card';
@Component({
  selector: 'app-my-team',
  imports: [FullCalendarModule, SharedModule, EmployeeCard],
  templateUrl: './my-team.html',
  styleUrl: './my-team.css',
})
export class MyTeam implements OnInit {

    @ViewChild('calendar') calendarComponent!: FullCalendarComponent;

    constructor(
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _userService: UserService,
        private _employeeService: EmployeeService
    ) {
    }
    leaveCounts: Record<string, { approved: number; pending: number; available: number }> = {};

    employeesLeaves: IEmployeesLeaves[] = [];
    employeesLeavesOnDate: IEmployeesLeaves[] = [];
    selectedDate: string | null = null;
    displayDate: boolean = false;
    selectedCounts: { approved: number; pending: number; available: number } | null = null;
    viewMode: 'grid' | 'list' = 'grid';
    totalRecords = 0;
    ngOnInit(): void {
        this.loadLeaveRequests();
    }

    calendarOptions = {
        plugins: FullCalendarPlugins,
        initialView: 'dayGridMonth',
        height: '100%',
        contentHeight: 'auto',
        expandRows: true,
        handleWindowResize: true,

        selectable: true,
        editable: false,

        dateClick: (info: any) => {
            const date = info.dateStr;
            this.selectedDate = date;
            this.selectedCounts = this.leaveCounts[date] || {
                approved: 0,
                pending: 0,
                available: this.employeesLeaves.length
            };
            this.displayDate = true;
            this.getLeavesForDate(info.date);
        },

        dayCellContent: (arg: any) => {
            const date = arg.date.toISOString().split('T')[0];
            const counts = this.leaveCounts[date] || { approved: 0, pending: 0, available: 0 };
            return {
            html: `
                <div class="flex flex-col items-center">
                <div>${arg.dayNumberText}</div>
                <div class="flex gap-2 text-xs mt-1">
                    ${counts.available > 0 ? `<span class="text-green-600 font-semibold">${counts.available}</span>` : ''}
                    ${counts.approved > 0 ? `<span class="text-red-600 font-semibold">${counts.approved}</span>` : ''}
                    ${counts.pending > 0 ? `<span class="text-orange-500 font-semibold">${counts.pending}</span>` : ''}
                </div>
                </div>
            `
            };
        }
    };


    loadLeaveRequests() {

        const user = this._userService.getUser();
        console.log("Current User:", user);
        /* if (!user||!user.departmentId) {
        console.error("User has no department_id");
        return;
        } */
        this._NgxSpinnerService.show();
        this._employeeService.getLeaveRequestPerDepartment(1).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {

                    this.employeesLeaves = res.data;
                    this.calculateLeaveCounts();
                    this.refreshCalendar();
                    this.show('success', 'Success', 'Leave requests loaded successfully');

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

    calculateLeaveCounts(): void {
        this.leaveCounts = {};

        // Get all unique dates from leave requests
        const allDates = new Set<string>();

        this.employeesLeaves.forEach(empLeave => {
            empLeave.leaveRequests.forEach(leave => {
                const start = new Date(leave.startDate);
                const end = new Date(leave.endDate);

                // Add all dates in the range
                for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
                    allDates.add(d.toISOString().split('T')[0]);
                }
            });
        });

        // Calculate counts for each date
        allDates.forEach(date => {
            const employeesOnApprovedLeave = new Set<string>();
            const employeesOnPendingLeave = new Set<string>();

            this.employeesLeaves.forEach(empLeave => {
                let hasApprovedLeave = false;
                let hasPendingLeave = false;

                empLeave.leaveRequests.forEach(leave => {
                    const start = new Date(leave.startDate).toISOString().split('T')[0];
                    const end = new Date(leave.endDate).toISOString().split('T')[0];
                    // Check if the date is within the leave range
                    if (date >= start && date <= end) {
                        if (leave.status.toLowerCase() === 'accepted') {
                            hasApprovedLeave = true;
                        } else if (leave.status.toLowerCase() === 'pending') {
                            hasPendingLeave = true;
                        }
                    }
                });

                if (hasApprovedLeave) {
                    employeesOnApprovedLeave.add(empLeave.employee.id);

                } else if (hasPendingLeave) {
                    employeesOnPendingLeave.add(empLeave.employee.id);
                }
            });

            const totalEmployees = this.employeesLeaves.length;
            const approvedCount = employeesOnApprovedLeave.size;
            const pendingCount = employeesOnPendingLeave.size;
            const availableCount = totalEmployees - approvedCount - pendingCount;

            this.leaveCounts[date] = {
                approved: approvedCount,
                pending: pendingCount,
                available: availableCount
            };
        });
    }

    refreshCalendar(): void {
    this.calendarOptions = {
        ...this.calendarOptions,
        dayCellContent: (arg: any) => {
            const date = arg.date.toISOString().split('T')[0];
            const counts = this.leaveCounts[date] || { approved: 0, pending: 0, available: 0 };
            return {
                html: `
                <div class="flex flex-col items-center">
                    <div>${arg.dayNumberText}</div>
                    <div class="flex gap-2 text-xs mt-1">
                        ${counts.available > 0 ? `<span class="text-green-600 font-semibold">${counts.available}</span>` : ''}
                        ${counts.approved > 0 ? `<span class="text-red-600 font-semibold">${counts.approved}</span>` : ''}
                        ${counts.pending > 0 ? `<span class="text-yellow-400 font-semibold">${counts.pending}</span>` : ''}
                    </div>
                </div>
                `
            };
        }
    };
    console.log(this.leaveCounts);
    }

    getLeavesForDate(date: Date): IEmployeesLeaves[]{
        const dateStr = date.toISOString().split('T')[0];
        this.employeesLeavesOnDate = this.employeesLeaves.filter(empLeave =>
            empLeave.leaveRequests.some(leave => {
                const start = new Date(leave.startDate).toISOString().split('T')[0];
                const end = new Date(leave.endDate).toISOString().split('T')[0];
                return dateStr >= start && dateStr <= end;
            })
        );
        return this.employeesLeavesOnDate;
    }


    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
