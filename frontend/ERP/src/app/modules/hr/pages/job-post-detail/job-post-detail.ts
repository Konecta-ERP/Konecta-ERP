import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import {
    JobPostService,
    JobPostDto,
    CreateJobPostDto,
    JobRequirement,
} from '../../../../core/services/job-post.service';
import {
    RecruitmentService,
    JobRequisitionDto,
} from '../../../../core/services/recruitment.service';
import { ApplicantService } from '../../../../core/services/applicant.service';
import { ApplicantDto } from '../../../../core/services/applicant.service';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
    selector: 'app-job-post-detail',
    imports: [CommonModule, FormsModule, SharedModule],
    templateUrl: './job-post-detail.html',
    styleUrl: './job-post-detail.css',
})
export class JobPostDetail implements OnInit {
    requisitionId: number | null = null;
    requisition: JobRequisitionDto | null = null;
    jobPost: JobPostDto | null = null;
    applicants: ApplicantDto[] = [];
    isLoading = false;
    showCreateModal = false;
    isViewMode = false;

    // Form fields for creating job post
    newJobPost: CreateJobPostDto = {
        title: '',
        description: '',
        requirements: [{ text: '', mandatory: false }],
        requisitionId: 0,
    };

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private jobPostService: JobPostService,
        private recruitmentService: RecruitmentService,
        @Inject(ApplicantService) private applicantService: ApplicantService,
        private messageService: MessageService,
        private spinner: NgxSpinnerService
    ) {}

    ngOnInit(): void {
        this.route.params.subscribe((params) => {
            if (params['requisitionId']) {
                this.requisitionId = +params['requisitionId'];
                this.loadRequisition();
                this.loadJobPostForRequisition();
            }
        });
    }

    /**
     * Load requisition details
     */
    loadRequisition(): void {
        if (!this.requisitionId) return;

        this.recruitmentService.getRequisition(this.requisitionId).subscribe({
            next: (res) => {
                if (res.status === 200) {
                    this.requisition = res.data;
                    this.newJobPost.requisitionId = this.requisitionId!;
                } else {
                    this.showMessage('error', 'Error', 'Failed to load requisition');
                }
            },
            error: (err) => {
                this.showMessage('error', 'Error', 'Error loading requisition');
            },
        });
    }

    /**
     * Load existing job post for this requisition
     */
    loadJobPostForRequisition(): void {
        if (!this.requisitionId) return;

        this.spinner.show();
        this.jobPostService.searchJobPosts(undefined, undefined, undefined).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 200 && res.data) {
                    const post = res.data.find((p) => p.requisitionId === this.requisitionId);
                    if (post) {
                        this.jobPost = post;
                        this.isViewMode = true;
                        this.loadApplicants(post.id);
                    }
                }
            },
            error: (err) => {
                this.spinner.hide();
            },
        });
    }

    /**
     * Load applicants for a job post
     */
    loadApplicants(postId: number): void {
        this.applicantService.getApplicantsForPost(postId).subscribe({
            next: (res) => {
                if (res.status === 200) {
                    this.applicants = res.data || [];
                } else {
                    this.showMessage('error', 'Error', 'Failed to load applicants');
                }
            },
            error: (err) => {
                this.showMessage('error', 'Error', 'Error loading applicants');
            },
        });
    }

    /**
     * Open create job post modal
     */
    openCreateModal(): void {
        this.resetForm();
        this.showCreateModal = true;
    }

    /**
     * Close create modal
     */
    closeCreateModal(): void {
        this.showCreateModal = false;
        this.resetForm();
    }

    /**
     * Reset form to defaults
     */
    private resetForm(): void {
        this.newJobPost = {
            title: '',
            description: '',
            requirements: [{ text: '', mandatory: false }],
            requisitionId: this.requisitionId || 0,
        };
    }

    /**
     * Add a new requirement field
     */
    addRequirement(): void {
        this.newJobPost.requirements.push({ text: '', mandatory: false });
    }

    /**
     * Remove a requirement field
     */
    removeRequirement(index: number): void {
        if (this.newJobPost.requirements.length > 1) {
            this.newJobPost.requirements.splice(index, 1);
        }
    }

    /**
     * Validate form
     */
    private validateForm(): boolean {
        if (!this.newJobPost.title || this.newJobPost.title.trim().length < 5) {
            this.showMessage('warn', 'Validation Error', 'Title must be at least 5 characters');
            return false;
        }
        if (this.newJobPost.title.length > 200) {
            this.showMessage('warn', 'Validation Error', 'Title cannot exceed 200 characters');
            return false;
        }
        if (!this.newJobPost.description || this.newJobPost.description.trim().length < 20) {
            this.showMessage(
                'warn',
                'Validation Error',
                'Description must be at least 20 characters'
            );
            return false;
        }
        if (this.newJobPost.description.length > 5000) {
            this.showMessage(
                'warn',
                'Validation Error',
                'Description cannot exceed 5000 characters'
            );
            return false;
        }
        const hasValidRequirements = this.newJobPost.requirements.some((r) => r.text.trim());
        if (!hasValidRequirements) {
            this.showMessage('warn', 'Validation Error', 'At least one requirement is required');
            return false;
        }
        return true;
    }

    /**
     * Submit job post creation
     */
    submitJobPost(): void {
        if (!this.validateForm()) {
            return;
        }

        // Filter out empty requirements
        const filteredRequirements = this.newJobPost.requirements.filter((r) => r.text.trim());

        const dto: CreateJobPostDto = {
            ...this.newJobPost,
            requirements: filteredRequirements,
        };

        this.spinner.show();
        this.jobPostService.createJobPost(dto).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 201 || res.status === 200) {
                    this.showMessage('success', 'Success', 'Job post created successfully!');
                    this.closeCreateModal();
                    this.loadJobPostForRequisition();
                    if (res.data && res.data.id) {
                        this.loadApplicants(res.data.id);
                    }
                } else {
                    this.showMessage('error', 'Error', res.cMessage || 'Failed to create job post');
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
     * Toggle job post active status
     */
    toggleJobPostActive(): void {
        if (!this.jobPost || !this.jobPost.id) return;

        this.spinner.show();
        this.jobPostService.setJobPostActive(this.jobPost.id, !this.jobPost.active).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 200) {
                    this.jobPost = res.data;
                    const action = res.data.active ? 'activated' : 'deactivated';
                    this.showMessage('success', 'Success', `Job post ${action} successfully!`);
                } else {
                    this.showMessage('error', 'Error', 'Failed to update job post status');
                }
            },
            error: (err) => {
                this.spinner.hide();
                this.showMessage('error', 'Error', 'Error updating job post status');
            },
        });
    }

    /**
     * Go back to recruitment page
     */
    goBack(): void {
        this.router.navigate(['/home/hr/recruitment']);
    }

    /**
     * Show message toast
     */
    private showMessage(severity: string, summary: string, detail: string): void {
        this.messageService.add({ severity, summary, detail, life: 3000 });
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
                hour: '2-digit',
                minute: '2-digit',
            });
        } catch {
            return dateString;
        }
    }

    /**
     * Open CV URL in new window
     */
    openCV(cvUrl: string): void {
        if (cvUrl) {
            window.open(cvUrl, '_blank');
        }
    }
}
