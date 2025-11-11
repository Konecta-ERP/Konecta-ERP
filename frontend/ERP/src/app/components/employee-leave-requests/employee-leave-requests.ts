import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import {  ILeaveRequestRequest } from '../../core/interfaces/iLeaveRequestRequest';
import { ILeaveRequestResponse } from '../../core/interfaces/iLeaveRequestResponse';
import { NgxSpinnerService } from 'ngx-spinner';
import { UserService } from '../../core/services/user.service';
import { EmployeeService } from '../../core/services/employee.service';
import { timeout } from 'rxjs/operators';
@Component({
  selector: 'app-employee-leave-requests',
  imports: [SharedModule],
  templateUrl: './employee-leave-requests.html',
  styleUrl: './employee-leave-requests.css',
  providers: [MessageService]
})
export class EmployeeLeaveRequests implements OnInit {
    constructor(
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _userService: UserService,
        private _employeeService: EmployeeService
    ) {
        this.initFormControls();
        this.initFormGroup();
    }
    leaveRequestForm!: FormGroup;
    period!: FormControl;
    reason!: FormControl;
    requestType!: FormControl;

    // Table data
    leaveRequests: ILeaveRequestResponse[] = [];
    filteredRequests: ILeaveRequestResponse[] = [];
    loading: boolean = false;


    // Dialog visibility
    displayDialog: boolean = false;
    displayViewDialog: boolean = false;
    selectedRequest: ILeaveRequestResponse | null = null;

    // Filter
    selectedStatus: string | null = null;
    statusOptions = [
        { label: 'All Statuses', value: null },
        { label: 'Pending', value: 'PENDING' },
        { label: 'Approved', value: 'APPROVED' },
        { label: 'Rejected', value: 'REJECTED' }
    ];

    requestTypes = [
        { name: 'Annual Leave', value: 'ANNUAL' },
        { name: 'Sick Leave', value: 'SICK' },
        { name: 'Personal Leave', value: 'PERSONAL' },
        { name: 'Emergency Leave', value: 'EMERGENCY' }
    ];

    ngOnInit() {
        this.loadLeaveRequests();
    }

    loadLeaveRequests() {
        this._NgxSpinnerService.show();
        this._employeeService.getLeaveRequests().subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {
                    this.leaveRequests = res.data;
                    this.filteredRequests = [...this.leaveRequests];
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

    // Show dialog for new request
    showDialog() {
        this.displayDialog = true;
        this.leaveRequestForm.reset();
    }

    // Hide dialog
    hideDialog() {
        this.displayDialog = false;
        this.leaveRequestForm.reset();
    }

    viewRequest(request: ILeaveRequestResponse) {
        this.selectedRequest = request;
        this.displayViewDialog = true;
    }

    deleteRequestAPI(request: ILeaveRequestResponse) {
        this._NgxSpinnerService.show();
        this._employeeService.deleteLeaveRequest(request.id).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 204) {
                    this.show('success', 'Success', res.cMessage || 'Leave request deleted successfully');
                    this.loadLeaveRequests();
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to delete leave request');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while deleting the leave request');
            }
        })

    }

    // Filter requests by status
    filterRequests() {
        if (this.selectedStatus) {
            this.filteredRequests = this.leaveRequests.filter(
                req => req.status === this.selectedStatus
            );
        } else {
            this.filteredRequests = [...this.leaveRequests];
        }
    }

    // Clear filters
    clearFilters() {
        this.selectedStatus = null;
        this.filteredRequests = [...this.leaveRequests];
    }

    // Get type name from value


    initFormControls() {
        this.period = new FormControl(null, [Validators.required]);
        this.reason = new FormControl('', [
            Validators.required,
            Validators.minLength(10),
            Validators.maxLength(500)
        ]);
        this.requestType = new FormControl(null, [Validators.required]);
    }

    initFormGroup() {
        this.leaveRequestForm = new FormGroup({
            period: this.period,
            reason: this.reason,
            requestType: this.requestType
        });
    }

    submit() {
        // Mark all fields as touched to trigger validation messages
        Object.keys(this.leaveRequestForm.controls).forEach(key => {
            this.leaveRequestForm.get(key)?.markAsTouched();
        });

        if (this.leaveRequestForm.valid) {
            // Validate that period has both start and end dates
            const periodValue = this.period.value;
            if (!periodValue || !periodValue[0] || !periodValue[1]) {
                this.show('error', 'Invalid Period', 'Please select both start and end dates for the leave period.');
                return;
            }

            // Check if end date is after start date
            if (periodValue[1] < periodValue[0]) {
                this.show('error', 'Invalid Period', 'End date must be after start date.');
                return;
            }
            const data :ILeaveRequestRequest = {
                startDate: periodValue[0],
                endDate: periodValue[1],
                reason: this.reason.value,
                requestType: this.requestType.value
            };

            this.requestLeaveAPI(data);

        } else {
            this.show('error', 'Error', 'Please correct the errors in the form before submitting.');
        }
    }

    requestLeaveAPI(data: ILeaveRequestRequest): void {
        this._NgxSpinnerService.show();

        this._employeeService.requestLeave(data)
        .pipe(timeout(3000))
        .subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status === 200) {
                    this.show('success', 'Success', res.cMessage || 'Leave request submitted successfully');
                    this.hideDialog();
                    this.loadLeaveRequests();
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to submit leave request');
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while submitting the leave request');
            },
        })
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

    // Getter methods for easier template access
    get periodControl() {
        return this.period;
    }

    get reasonControl() {
        return this.reason;
    }

    get requestTypeControl() {
        return this.requestType;
    }
}
