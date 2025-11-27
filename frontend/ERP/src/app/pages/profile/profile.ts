import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
import { EmployeeService } from '../../core/services/employee.service';
import { User } from '../../core/interfaces/iUser';
import format from '../../shared/functions/shared-functions';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-profile',
  imports: [SharedModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
    loggedInUser: User | null = null; // The logged-in user
    user: User | null = null;         // The profile being viewed

    formattedHireDate = 'N/A';
    activeTab = 'personal';

    // Permissions
    isHR = false;
    isFinance = false;
    canEditProfile = false;

    // Edit modes
    isEditingPersonal = false;
    isEditingEmployment = false;
    isEditingFinancial = false;

    // REACTIVE FORMS
    personalForm!: FormGroup;
    employmentForm!: FormGroup;
    financialForm!: FormGroup;

    tabs = [
        { id: 'personal', name: 'Personal Info' },
        { id: 'employment', name: 'Employment' },
    ];

    constructor(
        private _userService: UserService,
        private _employeeService: EmployeeService,
        private fb: FormBuilder,
        private route: ActivatedRoute
    ) {}

    ngOnInit() {
        this.loggedInUser = this._userService.getUser();
        const viewedUserId = this.route.snapshot.paramMap.get('id');

        if (viewedUserId) {
        // Viewing someone else’s profile
        this._employeeService.getEmployeeById(viewedUserId).subscribe((u) => {
            this.user = u;
            this.setupProfile();
        });
        } else {
        // Viewing own profile
        this.user = this.loggedInUser;
        this.setupProfile();
        }
    }

    private setupProfile() {
        if (!this.user) return;

        this.formattedHireDate = format(this.user.createdAt, 'date');
        this.checkPermissions();
        this.initializeForms();
    }

    private checkPermissions() {
        if (!this.loggedInUser) return;

        this.isHR =
        (this.loggedInUser.role === 'HR Manager' ||
        this.loggedInUser.role === 'HR Admin');

        this.isFinance =
        (this.loggedInUser.role === 'CFO' ||
        this.loggedInUser.role === 'Accountant');

        // Can edit if viewing own profile OR HR
        this.canEditProfile =
        (this.isHR ||
        this.loggedInUser.employeeId === this.user?.employeeId);
    }

    private initializeForms() {
        this.personalForm = this.fb.group({
        firstName: [this.user?.firstName || ''],
        lastName: [this.user?.lastName || ''],
        email: [this.user?.email || ''],
        phone: [this.user?.phone || ''],
        });

        this.employmentForm = this.fb.group({
        department: [this.user?.departmentName || ''],
        position: [this.user?.position || ''],
        });

        this.financialForm = this.fb.group({
        salaryNet: [this.user?.salaryNet || 0],
        salaryGross: [this.user?.salaryGross || ''],
        });
    }

    toggleEdit(section: string) {
        if (!this.canEditProfile) return;

        switch (section) {
        case 'personal':
            this.isEditingPersonal = !this.isEditingPersonal;
            if (!this.isEditingPersonal) {
            console.log('Saved personal info:', this.personalForm.value);
            }
            break;

        case 'employment':
            if (!this.isHR) return;
            this.isEditingEmployment = !this.isEditingEmployment;
            if (!this.isEditingEmployment) {
            console.log('Saved employment info:', this.employmentForm.value);
            }
            break;

        case 'financial':
            this.isEditingFinancial = !this.isEditingFinancial;
            if (!this.isEditingFinancial) {
            console.log('Financial info pending approval:', this.financialForm.value);
            }
            break;
        }
    }

    changeProfilePicture() {
        if (!this.canEditProfile) return;
        console.log('Change profile picture clicked');
    }

    getInputClasses(isEditable: boolean): string {
        const base = 'w-full px-3 py-2 border rounded-lg transition-colors';
        return isEditable
        ? `${base} border-blue-300 bg-white focus:border-blue-500 focus:ring-1 focus:ring-blue-500`
        : `${base} border-gray-300 bg-gray-50 text-gray-500 cursor-not-allowed`;
    }

    getTabClasses(tab: any): string {
        const base =
        'px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors';
        return this.activeTab === tab.id
        ? `${base} border-blue-500 text-blue-600`
        : `${base} border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300`;
    }
}
