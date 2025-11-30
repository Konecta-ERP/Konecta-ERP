import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SharedModule } from '../../../../shared/module/shared/shared-module';
import {
    RecruitmentService,
    JobRequisitionDto,
    CreateRequisitionDto,
} from '../../../../core/services/recruitment.service';
import { DepartmentService } from '../../../../core/services/department.service';
import { UserService } from '../../../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { IDepartment } from '../../../../core/interfaces/iDepartment';
import { User } from '../../../../core/interfaces/iUser';
import {
    canApproveRequisitions,
    canPerformRecruitmentActions,
    canViewAndCreateRequisitions,
} from '../../../../core/constants/roles';

interface StatusOption {
    label: string;
    value: string;
    severity: 'success' | 'info' | 'warn' | 'danger' | 'secondary';
}

interface PriorityOption {
    label: string;
    value: string;
    severity: 'success' | 'info' | 'warn' | 'danger' | 'secondary';
}

@Component({
    selector: 'app-recruitment',
    imports: [CommonModule, FormsModule, SharedModule],
    templateUrl: './recruitment.html',
    styleUrl: './recruitment.css',
})
export class Recruitment implements OnInit {
    requisitions: JobRequisitionDto[] = [];
    isLoading = false;
    showCreateModal = false;
    currentUser: User | null = null;

    // Form fields
    newRequisition: CreateRequisitionDto = {
        reason: '',
        priority: 'MEDIUM',
        openings: 1,
        departmentId: undefined as any,
    };

    // Dropdown options
    departments: IDepartment[] = [];
    dropDownDepartments: { label: string; value: number }[] = [];
    priorityOptions: PriorityOption[] = [
        { label: 'Low', value: 'LOW', severity: 'info' },
        { label: 'Medium', value: 'MEDIUM', severity: 'warn' },
        { label: 'High', value: 'HIGH', severity: 'danger' },
    ];

    statusMap: Map<string, StatusOption> = new Map([
        ['PENDING', { label: 'Pending', value: 'PENDING', severity: 'warn' }],
        ['APPROVED', { label: 'Approved', value: 'APPROVED', severity: 'success' }],
        ['REJECTED', { label: 'Rejected', value: 'REJECTED', severity: 'danger' }],
    ]);

    constructor(
        private recruitmentService: RecruitmentService,
        private departmentService: DepartmentService,
        private userService: UserService,
        private router: Router,
        private messageService: MessageService,
        private spinner: NgxSpinnerService
    ) {}

    ngOnInit(): void {
        this.currentUser = this.userService.getUser();
        this.loadDepartments();
        this.loadRequisitions();
    }

    /**
     * Load departments for dropdown
     */
    loadDepartments(): void {
        if (this.departmentService.hasCachedDepartments()) {
            this.departments = this.departmentService.getCachedDepartments();
            this.buildDepartmentDropdown();
        } else {
            this.departmentService.getDepartments().subscribe({
                next: (res) => {
                    if (res.status === 200 && res.data) {
                        this.departmentService.setDepartments(res.data);
                        this.departments = this.departmentService.getCachedDepartments();
                        this.buildDepartmentDropdown();
                    }
                },
                error: (err) => {
                    this.showMessage('error', 'Error', 'Failed to load departments');
                },
            });
        }
    }

    /**
     * Build dropdown options from departments
     */
    private buildDepartmentDropdown(): void {
        this.dropDownDepartments = this.departments.map((dept) => ({
            label: dept.name,
            value: dept.id,
        }));
    }

    /**
     * Load all job requisitions
     */
    loadRequisitions(): void {
        this.spinner.show();
        this.isLoading = true;
        this.recruitmentService.searchRequisitions().subscribe({
            next: (res) => {
                this.spinner.hide();
                this.isLoading = false;
                if (res.status === 200) {
                    this.requisitions = res.data || [];
                } else {
                    this.showMessage(
                        'error',
                        'Error',
                        res.cMessage || 'Failed to load requisitions'
                    );
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
     * Open the create modal
     */
    openCreateModal(): void {
        this.resetForm();
        this.showCreateModal = true;
    }

    /**
     * Close the create modal
     */
    closeCreateModal(): void {
        this.showCreateModal = false;
        this.resetForm();
    }

    /**
     * Reset form to defaults
     */
    private resetForm(): void {
        this.newRequisition = {
            reason: '',
            priority: 'MEDIUM',
            openings: 1,
            departmentId: undefined as any,
        };
    }

    /**
     * Validate form fields
     */
    private validateForm(): boolean {
        if (!this.newRequisition.reason || this.newRequisition.reason.trim().length < 10) {
            this.showMessage('warn', 'Validation Error', 'Reason must be at least 10 characters');
            return false;
        }
        if (this.newRequisition.reason.length > 500) {
            this.showMessage('warn', 'Validation Error', 'Reason cannot exceed 500 characters');
            return false;
        }
        if (!this.newRequisition.priority) {
            this.showMessage('warn', 'Validation Error', 'Priority is required');
            return false;
        }
        if (!this.newRequisition.openings || this.newRequisition.openings < 1) {
            this.showMessage('warn', 'Validation Error', 'Number of openings must be at least 1');
            return false;
        }
        if (!this.newRequisition.departmentId) {
            this.showMessage('warn', 'Validation Error', 'Department is required');
            return false;
        }
        return true;
    }

    /**
     * Submit new requisition
     */
    submitRequisition(): void {
        if (!this.validateForm()) {
            return;
        }

        this.spinner.show();
        this.recruitmentService.createRequisition(this.newRequisition).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 201 || res.status === 200) {
                    this.showMessage('success', 'Success', 'Job requisition created successfully!');
                    this.closeCreateModal();
                    this.loadRequisitions();
                } else {
                    this.showMessage(
                        'error',
                        'Error',
                        res.cMessage || 'Failed to create requisition'
                    );
                }
            },
            error: (err) => {
                this.spinner.hide();
                const errorMsg =
                    err?.error?.cMessage ||
                    err?.error?.message ||
                    'An error occurred while creating the requisition';
                this.showMessage('error', 'Error', errorMsg);
            },
        });
    }

    /**
     * Get status badge properties
     */
    getStatusOptions(status: string): StatusOption {
        return (
            this.statusMap.get(status) || { label: status, value: status, severity: 'secondary' }
        );
    }

    /**
     * Get priority badge properties
     */
    getPriorityOptions(priority: string): PriorityOption {
        return (
            this.priorityOptions.find((p) => p.value === priority) || {
                label: priority,
                value: priority,
                severity: 'secondary',
            }
        );
    }

    /**
     * Get department name by ID
     */
    getDepartmentName(departmentId: number): string {
        return this.departments.find((d) => d.id === departmentId)?.name || 'Unknown';
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
            });
        } catch {
            return dateString;
        }
    }

    /**
     * Get pending requisitions count
     */
    getPendingCount(): number {
        return this.requisitions.filter((r) => r.status === 'PENDING').length;
    }

    /**
     * Get approved requisitions count
     */
    getApprovedCount(): number {
        return this.requisitions.filter((r) => r.status === 'APPROVED').length;
    }

    /**
     * Get total openings across all requisitions
     */
    getTotalOpenings(): number {
        return this.requisitions.reduce((sum, r) => sum + r.openings, 0);
    }

    /**
     * Check if user can approve/reject requisitions (HR_MANAGER or ADMIN only)
     */
    canApproveRequisitions(): boolean {
        return canApproveRequisitions(this.currentUser?.role);
    }

    /**
     * Check if user can perform recruitment actions (all HR roles and ADMIN)
     */
    canPerformRecruitmentActions(): boolean {
        return canPerformRecruitmentActions(this.currentUser?.role);
    }

    /**
     * Check if user can view/create requisitions (HR roles, managers, CFO, or ADMIN)
     */
    canViewAndCreateRequisitions(): boolean {
        return canViewAndCreateRequisitions(this.currentUser?.role);
    }

    /**
     * Approve a requisition
     */
    approveRequisition(requisition: JobRequisitionDto): void {
        if (!requisition.id) return;

        this.spinner.show();
        this.recruitmentService
            .updateRequisitionStatus(requisition.id, { status: 'APPROVED' })
            .subscribe({
                next: (res) => {
                    this.spinner.hide();
                    if (res.status === 200 || res.status === 201) {
                        this.showMessage(
                            'success',
                            'Success',
                            'Requisition approved successfully!'
                        );
                        this.loadRequisitions();
                    } else {
                        this.showMessage(
                            'error',
                            'Error',
                            res.cMessage || 'Failed to approve requisition'
                        );
                    }
                },
                error: (err) => {
                    this.spinner.hide();
                    this.showMessage(
                        'error',
                        'Error',
                        err?.error?.cMessage || 'An error occurred while approving'
                    );
                },
            });
    }

    /**
     * Reject a requisition (deletes it from the system)
     */
    rejectRequisition(requisition: JobRequisitionDto): void {
        if (!requisition.id) return;

        this.spinner.show();
        this.recruitmentService.deleteRequisition(requisition.id).subscribe({
            next: (res) => {
                this.spinner.hide();
                if (res.status === 200 || res.status === 204) {
                    this.showMessage(
                        'success',
                        'Success',
                        'Requisition rejected and deleted successfully!'
                    );
                    this.loadRequisitions();
                } else {
                    this.showMessage(
                        'error',
                        'Error',
                        res.cMessage || 'Failed to reject requisition'
                    );
                }
            },
            error: (err) => {
                this.spinner.hide();
                this.showMessage(
                    'error',
                    'Error',
                    err?.error?.cMessage || 'An error occurred while rejecting'
                );
            },
        });
    }

    /**
     * Navigate to job post detail page for a requisition
     */
    navigateToJobPost(requisitionId: number): void {
        this.router.navigate(['/home/hr/recruitment/job-post', requisitionId]);
    }
}
