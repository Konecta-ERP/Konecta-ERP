import { Component } from '@angular/core';
import { PerformanceReviewCard } from '../performance-review-card/performance-review-card';
import { UserService } from '../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { IFeedbackResponse } from '../../core/interfaces/i-feedback-response';
import { EmployeeService } from '../../core/services/employee.service';
@Component({
  selector: 'app-employee-performance-reviews',
  imports: [PerformanceReviewCard, SharedModule],
  templateUrl: './employee-performance-reviews.html',
  styleUrl: './employee-performance-reviews.css',
})
export class EmployeePerformanceReviews {
        reviews: IFeedbackResponse[] = [];

    constructor(
        private _userService: UserService,
        private _messageService: MessageService,
        private _spinner: NgxSpinnerService,
        private _employeeService: EmployeeService
    ) {}

    ngOnInit(): void {
        this.loadPerformanceReviews();
    }

    loadPerformanceReviews(): void {
        console.log('Loading performance reviews...');
        this._spinner.show();

        this._employeeService.getPerformanceReviews().subscribe({
        next: (res) => {
            this._spinner.hide();
            if (res.status === 200) {
            this.reviews = res.data;
            } else {
            this.show(
                'error',
                'Error',
                res.cMessage || 'Failed to load performance reviews'
            );
            }
        },
        error: (err) => {
            this._spinner.hide();
            this.show(
            'error',
            'Error',
            err?.error?.cMessage ||
                'An error occurred while loading performance reviews'
            );
        },
        });
     }

    show(
        severity: string = 'info',
        summary: string = 'Info',
        detail: string = 'Message Content'
    ): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
