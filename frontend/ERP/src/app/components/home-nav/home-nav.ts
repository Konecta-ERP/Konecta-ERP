import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { MenuItem, MessageService } from 'primeng/api';
import { RouterLink } from "@angular/router";
import { UserService } from '../../core/services/user.service';
import { AttendanceService } from '../../core/services/attendance.service';
import { User } from '../../core/interfaces/iUser';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'app-home-nav',
  imports: [SharedModule, RouterLink],
  templateUrl: './home-nav.html',
  styleUrl: './home-nav.css',
})
export class HomeNav implements OnInit {
    items: MenuItem[] | undefined;
    userMenuItems: MenuItem[] = [];
    isClockedIn: boolean = false;
    isCheckingStatus: boolean = false;
    currentUser: User | null = null;

    constructor(
        private _userService: UserService,
        private _attendanceService: AttendanceService,
        private _messageService: MessageService,
        private _spinner: NgxSpinnerService
    ){}
    ngOnInit() {
        this.currentUser = this._userService.getUser();
        this.checkAttendanceStatus();
        
        this.items = [
            {
                label: 'Dashboard',
                path: 'dashboard'
            },
            {
                label: 'HR',
                path: 'hr'
            }
        ];

        this.userMenuItems = [
            {
                label: 'Profile',
                icon: 'pi pi-user',
                routerLink: 'profile',
            },
            {
                label: 'Logout',
                icon: 'pi pi-sign-out',
                command: () => this.logout(),
            },
        ];

        if (this._userService.fromFinanceDepartment()) {
            this.items.push({
                label: 'Finance',
                path: 'finance'
            });
        }

    }



    logout() {
        if(this._userService.logout()){
            window.location.reload();
        }
    }

    /**
     * Check current attendance status
     */
    checkAttendanceStatus(): void {
        if (!this.currentUser?.employeeId) {
            return;
        }

        this.isCheckingStatus = true;
        this._attendanceService.getLatestAttendance(Number(this.currentUser.employeeId)).subscribe({
            next: (res) => {
                this.isCheckingStatus = false;
                if (res.status === 200 && res.data) {
                    // If clockOutTime is null, user is clocked in
                    this.isClockedIn = res.data.clockOutTime === null;
                } else {
                    this.isClockedIn = false;
                }
            },
            error: (err) => {
                this.isCheckingStatus = false;
                this.isClockedIn = false;
            },
        });
    }

    /**
     * Handle clock in/out action
     */
    onClockAction(): void {
        if (!this.currentUser?.employeeId) {
            this.showMessage('error', 'Error', 'Employee ID not found');
            return;
        }

        const employeeId = Number(this.currentUser.employeeId);
        const action = this.isClockedIn ? 'out' : 'in';

        this._spinner.show();
        const request = this.isClockedIn
            ? this._attendanceService.clockOut(employeeId)
            : this._attendanceService.clockIn(employeeId);

        request.subscribe({
            next: (res) => {
                this._spinner.hide();
                if (res.status === 200) {
                    this.isClockedIn = !this.isClockedIn;
                    this.showMessage(
                        'success',
                        'Success',
                        `Successfully clocked ${action}!`
                    );
                } else {
                    this.showMessage('error', 'Error', res.cMessage || `Failed to clock ${action}`);
                }
            },
            error: (err) => {
                this._spinner.hide();
                this.showMessage(
                    'error',
                    'Error',
                    err?.error?.cMessage || `An error occurred while clocking ${action}`
                );
            },
        });
    }

    /**
     * Show toast message
     */
    private showMessage(severity: string, summary: string, detail: string): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
