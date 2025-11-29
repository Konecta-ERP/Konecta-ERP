import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { JobPostService, JobPostDto } from '../../core/services/job-post.service';
import { ApplicantService, ApplicantApplicationDto } from '../../core/services/applicant.service';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';

interface JobRequirement {
    text: string;
    mandatory: boolean;
}

@Component({
    selector: 'app-job-portal',
    imports: [CommonModule, FormsModule, SharedModule],
    templateUrl: './job-portal.html',
    styleUrl: './job-portal.css',
})
export class JobPortal implements OnInit {
    jobPosts: JobPostDto[] = [];
    isLoading = false;
    showApplicationModal = false;
    selectedJob: JobPostDto | null = null;

    applicationForm = {
        firstName: '',
        lastName: '',
        email: '',
        coverLetter: '',
        cvUrl: '',
    };

    constructor(
        private jobPostService: JobPostService,
        private applicantService: ApplicantService,
        private messageService: MessageService,
        private spinner: NgxSpinnerService
    ) {}

    ngOnInit(): void {
        this.loadJobPosts();
    }

    /**
     * Load all active job posts
     */
    loadJobPosts(): void {
        this.spinner.show();
        this.isLoading = true;
        this.jobPostService.searchJobPosts(undefined, undefined, true).subscribe({
            next: (res) => {
                this.spinner.hide();
                this.isLoading = false;
                if (res.status === 200) {
                    this.jobPosts = res.data || [];
                } else {
                    this.showMessage('error', 'Error', 'Failed to load job posts');
                }
            },
            error: (err) => {
                this.spinner.hide();
                this.isLoading = false;
                this.showMessage('error', 'Error', err?.error?.cMessage || 'An error occurred');
            },
        });
    }

    /**
     * Open application modal for a job post
     */
    openApplicationModal(jobPost: JobPostDto): void {
        this.selectedJob = jobPost;
        this.resetApplicationForm();
        this.showApplicationModal = true;
    }

    /**
     * Close application modal
     */
    closeApplicationModal(): void {
        this.showApplicationModal = false;
        this.selectedJob = null;
        this.resetApplicationForm();
    }

    /**
     * Reset application form
     */
    private resetApplicationForm(): void {
        this.applicationForm = {
            firstName: '',
            lastName: '',
            email: '',
            coverLetter: '',
            cvUrl: '',
        };
    }

    /**
     * Validate application form
     */
    private validateForm(): boolean {
        if (!this.applicationForm.firstName || this.applicationForm.firstName.trim().length < 2) {
            this.showMessage(
                'warn',
                'Validation Error',
                'First name must be at least 2 characters'
            );
            return false;
        }
        if (!this.applicationForm.lastName || this.applicationForm.lastName.trim().length < 2) {
            this.showMessage('warn', 'Validation Error', 'Last name must be at least 2 characters');
            return false;
        }
        if (!this.applicationForm.email || !this.isValidEmail(this.applicationForm.email)) {
            this.showMessage('warn', 'Validation Error', 'Please enter a valid email address');
            return false;
        }
        if (!this.applicationForm.cvUrl || this.applicationForm.cvUrl.trim().length < 5) {
            this.showMessage('warn', 'Validation Error', 'CV URL must be provided');
            return false;
        }
        if (
            !this.applicationForm.coverLetter ||
            this.applicationForm.coverLetter.trim().length < 20
        ) {
            this.showMessage(
                'warn',
                'Validation Error',
                'Cover letter must be at least 20 characters'
            );
            return false;
        }
        return true;
    }

    /**
     * Validate email format
     */
    private isValidEmail(email: string): boolean {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    /**
     * Submit application
     */
    submitApplication(): void {
        if (!this.validateForm() || !this.selectedJob || !this.selectedJob.id) {
            return;
        }

        const dto: ApplicantApplicationDto = {
            firstName: this.applicationForm.firstName.trim(),
            lastName: this.applicationForm.lastName.trim(),
            email: this.applicationForm.email.trim(),
            cvUrl: this.applicationForm.cvUrl.trim(),
            coverLetter: this.applicationForm.coverLetter.trim(),
            postId: this.selectedJob.id,
        };

        this.spinner.show();
        this.jobPostService.submitApplication(this.selectedJob.id, dto).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 201 || res.status === 200) {
                    this.showMessage(
                        'success',
                        'Success',
                        'Your application has been submitted successfully!'
                    );
                    this.closeApplicationModal();
                    this.loadJobPosts();
                } else {
                    this.showMessage(
                        'error',
                        'Error',
                        res.cMessage || 'Failed to submit application'
                    );
                }
            },
            error: (err) => {
                this.spinner.hide();
                const errorMsg = err?.error?.cMessage || err?.error?.message || 'An error occurred';
                this.showMessage('error', 'Error', errorMsg);
            },
        });
    }

    /**
     * Format date
     */
    formatDate(dateString: string): string {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
            });
        } catch {
            return dateString;
        }
    }

    /**
     * Show message toast
     */
    private showMessage(severity: string, summary: string, detail: string): void {
        this.messageService.add({ severity, summary, detail, life: 3000 });
    }
}
