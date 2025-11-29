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
        cvFile: null as File | null,
        cvFileName: '',
    };

    isDragOver = false;

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
            cvFile: null,
            cvFileName: '',
        };
    }

    /**
     * Handle file selection from input
     */
    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.handleFileUpload(input.files[0]);
        }
    }

    /**
     * Handle drag over event
     */
    onDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = true;
    }

    /**
     * Handle drag leave event
     */
    onDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = false;
    }

    /**
     * Handle drop event
     */
    onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = false;

        if (event.dataTransfer && event.dataTransfer.files.length > 0) {
            this.handleFileUpload(event.dataTransfer.files[0]);
        }
    }

    /**
     * Process the uploaded file
     */
    private handleFileUpload(file: File): void {
        // Validate file type
        if (!file.name.endsWith('.pdf') && !file.name.endsWith('.docx')) {
            this.showMessage(
                'warn',
                'Invalid File',
                'Only PDF and DOCX files are allowed. Please upload a valid CV.'
            );
            return;
        }

        // Validate file size (max 10MB)
        const maxSize = 10 * 1024 * 1024;
        if (file.size > maxSize) {
            this.showMessage('warn', 'File Too Large', 'CV file must be smaller than 10MB.');
            return;
        }

        this.applicationForm.cvFile = file;
        this.applicationForm.cvFileName = file.name;
        this.showMessage('success', 'File Uploaded', `${file.name} uploaded successfully!`);
    }

    /**
     * Remove uploaded CV file
     */
    removeCvFile(): void {
        this.applicationForm.cvFile = null;
        this.applicationForm.cvFileName = '';
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
        if (!this.applicationForm.cvFile) {
            this.showMessage('warn', 'Validation Error', 'Please upload your CV');
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
        if (
            !this.validateForm() ||
            !this.selectedJob ||
            !this.selectedJob.id ||
            !this.applicationForm.cvFile
        ) {
            return;
        }

        const dto: ApplicantApplicationDto = {
            firstName: this.applicationForm.firstName.trim(),
            lastName: this.applicationForm.lastName.trim(),
            email: this.applicationForm.email.trim(),
            coverLetter: this.applicationForm.coverLetter.trim(),
            postId: this.selectedJob.id,
        };

        this.spinner.show();
        this.applicantService
            .submitApplication(this.selectedJob.id, dto, this.applicationForm.cvFile)
            .subscribe({
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
                    const errorMsg =
                        err?.error?.cMessage || err?.error?.message || 'An error occurred';
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
