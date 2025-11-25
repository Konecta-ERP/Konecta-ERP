import { Component } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/interfaces/iUser';
import format from '../../shared/functions/shared-functions';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-profile',
  imports: [SharedModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  user: User | null = null;
  formattedHireDate: string = 'N/A';

  activeTab = 'personal';

  // Permissions
  isHR = false;
  isFinance = false;
  canEditProfile = false;

  // Edit states
  isEditingPersonal = false;
  isEditingEmployment = false;
  isEditingFinancial = false;

  // Form groups
  personalForm!: FormGroup;
  financialForm!: FormGroup;

  // Employment info (could come from the user object or a related model)
  employmentInfo = {
    department: '',
    position: '',
  };

  tabs = [
    { id: 'personal', name: 'Personal Info' },
    { id: 'employment', name: 'Employment' },
  ];

  constructor(
    private _userService: UserService,
    private fb: FormBuilder
  ) {
    this.user = this._userService.getUser();

    if (this.user) {
      this.formattedHireDate = format(this.user.createdAt, 'date');
      /* this.employmentInfo = {
        department: this.user.department || '',
        position: this.user.position || '',
      }; */
    }
  }

  ngOnInit() {
    this.checkPermissions();
    this.initializeForms();
  }

  private checkPermissions() {
    if (!this.user) return;

    this.isHR = this.user.role === 'HR Manager' || this.user.role === 'HR Admin';
    this.isFinance = this.user.role === 'CFO' || this.user.role === 'Accountant';
    this.canEditProfile = this.isHR || this.user.role === 'Employee';
  }

  private initializeForms() {
    this.personalForm = this.fb.group({
      email: [this.user?.email || ''],
      phone: [this.user?.phone || ''],
      firstName: [this.user?.firstName || ''],
      lastName: [this.user?.lastName || ''],
    });
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

  toggleEdit(section: string) {
    if (section === 'personal') {
      this.isEditingPersonal = !this.isEditingPersonal;

      if (!this.isEditingPersonal) {
        // Save logic for personal info
        const updated = this.personalForm.value;
        console.log('Saved personal info:', updated);
      }
    } else if (section === 'employment' && this.isHR) {
      this.isEditingEmployment = !this.isEditingEmployment;

      if (!this.isEditingEmployment) {
        console.log('Saved employment info:', this.employmentInfo);
      }
    } else if (section === 'financial') {
      this.isEditingFinancial = !this.isEditingFinancial;

      if (!this.isEditingFinancial) {
        const updated = this.financialForm.value;
        console.log('Financial info pending approval:', updated);
      }
    }
  }

  changeProfilePicture() {
    console.log('Change profile picture clicked');
  }
}
