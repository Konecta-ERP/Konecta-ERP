import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
import { EmployeeService } from '../../core/services/employee.service';
import { User } from '../../core/interfaces/iUser';
import format from '../../shared/functions/shared-functions';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { IFeedbackRequest } from '../../core/interfaces/iFeedbackRequest';
import { IFeedbackResponse } from '../../core/interfaces/i-feedback-response';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { DepartmentService } from '../../core/services/department.service';
@Component({
  selector: 'app-profile',
  imports: [SharedModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
    loggedInUser: User | null = null;
    user: User | null = null;

    formattedHireDate = 'N/A';

    // Permissions
    isHR = false;
    canViewSalary = false;
    canGiveFeedback = true;
    canAddGoals = false;

    // Dialog states
    showUserEditDialog = false;
    showHREditDialog = false;
    showGoalsDialog = false;
    showFeedbackDialog = false;

    // Forms
    userEditForm!: FormGroup;
    hrEditForm!: FormGroup;
    feedBackForm!: FormGroup;
    goalsForm!: FormGroup;

    // feedbacks
    feedbacksReceived: IFeedbackResponse[] = [];

    constructor(
        private _userService: UserService,
        private _employeeService: EmployeeService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _departmentService: DepartmentService
    ) {
        this.initializeForms();
    }

    ngOnInit() {
        this.loggedInUser = this._userService.getUser();
        const viewedUserId = this.route.snapshot.paramMap.get('id');

        if (viewedUserId) {
            this.loadUser(viewedUserId);
            this.loadGoals(viewedUserId);
        } else {
            this.user = this.loggedInUser;
            this.setupProfile();
        }

    }

    private loadUser(viewedUserId:string): void{
        this._NgxSpinnerService.show();
        this._employeeService.getEmployeeById(viewedUserId).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();
                if (res.status==200) {
                    this.user = {
                        ...res.data,
                        id: res.data.userId,
                        position: res.data.positionTitle,
                        role: 'employee',
                        departmentId: this._departmentService.getDepartmentIdByName(res.data.departmentName || '')
                    }
                    this.show('success','Success','User profile loaded successfully.');
                    this.setupProfile();
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();
                this.show('error','Error','Failed to load user profile.');
            }
        });
    }

    private initializeForms() {
        // User can edit: email, phone
        this.userEditForm = this.fb.group({
            phone: ['', [Validators.required, Validators.minLength(11), Validators.maxLength(11)]],
        });

        // HR can edit: firstName, lastName, department, position, salary
        this.hrEditForm = this.fb.group({
            position: [''],
            salaryNet: [0],
            salaryGross: [0],
        });

        // feedback form
        this.feedBackForm = this.fb.group({
            content: ['', Validators.required]
        });

        // goals form
        this.goalsForm = this.fb.group({
            title: ['', Validators.required],
            description: ['', Validators.required],
            target: ['', Validators.required],
            cycle: ['', Validators.required],
        });
    }

    private setupProfile() {
        if (!this.user) return;

        this.formattedHireDate = format(this.user.createdAt, 'date');
        this.checkPermissions();
        this.populateForms();
    }

    private checkPermissions() {
        if (!this.loggedInUser) return;

        this.isHR = (this.loggedInUser.role === 'HR_MANAGER' ||
                     this.loggedInUser.role === 'ADMIN'
                    ) && this.loggedInUser.employeeId !== this.user?.employeeId;

        this.canAddGoals = (this.loggedInUser.role === 'MANAGER' ||(this.loggedInUser.role === 'HR_MANAGER' && this.user?.departmentName === this.loggedInUser.departmentName))

        // Can view salary if: own profile OR HR
        this.canViewSalary = (this.isHR ||
                             this.loggedInUser.employeeId === this.user?.employeeId);

        this.canGiveFeedback = this.loggedInUser.employeeId !== this.user?.employeeId;
    }

    private populateForms() {
        this.userEditForm.patchValue({
            phoneNumber: this.user?.phone || '',
        });

        this.hrEditForm.patchValue({
            position: this.user?.position || '',
            salaryNet: this.user?.salaryNet || 0,
            salaryGross: this.user?.salaryGross || 0,
        });
    }

    // Dialog handlers
    openUserEditDialog() {
        this.populateForms();
        this.showUserEditDialog = true;
    }

    closeUserEditDialog() {
        this.showUserEditDialog = false;
    }

    saveUserEdit() {

        if (this.user?.employeeId){
        if (this.userEditForm.valid) {
            console.log('User edit saved:', this.userEditForm.value);
            this._NgxSpinnerService.show();
            const updatedData = this.userEditForm.value;
            this._employeeService.updateEmployeeDetails(this.user!?.employeeId, updatedData).subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        this.show('success','Success','Profile updated successfully.');
                    } else {
                        this.show('error','Error','Failed to update profile.');
                    }
                },
                error: (err) => {
                    this._NgxSpinnerService.hide();
                    this.show('error','Error','Failed to update profile.');
                }
            });
            this.closeUserEditDialog();
        }}

    }

    openHREditDialog() {
        this.populateForms();
        this.showHREditDialog = true;
    }

    closeHREditDialog() {
        this.showHREditDialog = false;
    }

    saveHREdit() {
        if (this.hrEditForm.valid) {

            this._NgxSpinnerService.show();
            const updatedData = {
                positionTitle: this.hrEditForm.value.position,
                salaryNet: this.hrEditForm.value.salaryNet,
                salaryGross: this.hrEditForm.value.salaryGross,
            }
            this._employeeService.updateEmployeeDetails(this.user!?.employeeId!, updatedData).subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        this.show('success','Success','Profile updated successfully.');
                        this.ngOnInit();
                    } else {
                        this.show('error','Error','Failed to update profile.');
                    }
                },error: (err) => {
                    this._NgxSpinnerService.hide();
                    this.show('error','Error','Failed to update profile.');
                }
            });

            this.closeHREditDialog();
        }
    }


    // feedback handlers

    openFeedbackDialog(): void{
        this.initializeForms();
        this.showFeedbackDialog = true;
    }

    closeFeedbackDialog(): void{
        this.showFeedbackDialog = false;
    }

    submitFeedback(): void{
        if(this.feedBackForm.valid && this.user?.employeeId){
            const feedbackContent = this.feedBackForm.value.content;
            this._NgxSpinnerService.show();
            this._employeeService.submitFeedback(this.user.employeeId,this.loggedInUser?.employeeId!, feedbackContent).subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 201) {
                        this.show('success','Success','Feedback submitted successfully.');
                        this.feedBackForm.reset();
                    } else {
                        this.show('error','Error','Failed to submit feedback.');
                    }
                },
                error: (err) => {
                    this._NgxSpinnerService.hide();
                    this.show('error','Error','Failed to submit feedback.');
                }
            });
        }
    }

    //goals handler

    openGoalsDialog(): void{
        this.showGoalsDialog = true;
    }

    closeGoalsDialog(): void{
        this.showGoalsDialog = false;
    }

    loadGoals(viewedUserId:string): void{
    }

    submitGoal(): void {
        if (this.goalsForm.valid && this.user?.employeeId) {
            this._NgxSpinnerService.show();
            const goalData = this.goalsForm.value;
            this._employeeService.submitGoal(this.user.employeeId, goalData).subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 201) {
                        this.show('success', 'Success', 'Goal submitted successfully.');
                        this.goalsForm.reset();
                        this.closeGoalsDialog();
                    } else {
                        this.show('error', 'Error', 'Failed to submit goal.');
                    }
                },
                error: (err) => {
                    this._NgxSpinnerService.hide();
                    this.show('error', 'Error', 'Failed to submit goal.');
                }
            });
        }
    }

    changeProfilePicture() {
        // TODO
    }

    formatCommentDate(date: Date): string {
        return format(date, 'date');
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }
}
