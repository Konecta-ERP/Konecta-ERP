import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ILogin } from '../../core/interfaces/ilogin';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
import { DepartmentService } from '../../core/services/department.service';
import { EmployeeService } from '../../core/services/employee.service';

@Component({
    selector: 'app-login',
    imports: [SharedModule],
    templateUrl: './login.html',
    styleUrl: './login.css',
})
export class Login {
    constructor(
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _userService: UserService,
        private _departmentService: DepartmentService,
        private _employeeService: EmployeeService,
        private _router: Router
    ) {
        this.initFormControls();
        this.initFormGroup();
    }
    email!: FormControl;
    password!: FormControl;
    loginForm!: FormGroup;
    showPassword = false;

    // Forgot Password Flow
    currentStep: 'login' | 'forgot-password-email' | 'otp' | 'reset' = 'login';
    forgotPasswordEmail: string = '';
    resetToken: string = '';
    forgotPasswordForm!: FormGroup;
    forgotPasswordEmailControl!: FormControl;
    otpForm!: FormGroup;
    resetPasswordForm!: FormGroup;
    otpCode!: FormControl;
    newPassword!: FormControl;
    confirmPassword!: FormControl;
    showNewPassword = false;
    showConfirmPassword = false;
    resendTimer = 0;
    resendDisabled = false;

    togglePassword() {
        this.showPassword = !this.showPassword;
    }

    toggleNewPassword() {
        this.showNewPassword = !this.showNewPassword;
    }

    toggleConfirmPassword() {
        this.showConfirmPassword = !this.showConfirmPassword;
    }

    initFormControls() {
        this.email = new FormControl('', [Validators.required, Validators.email]);
        this.password = new FormControl('', [Validators.required]);
        this.forgotPasswordEmailControl = new FormControl('', [
            Validators.required,
            Validators.email,
        ]);
        this.otpCode = new FormControl('', [Validators.required, Validators.minLength(6)]);
        this.newPassword = new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            this.passwordStrengthValidator.bind(this),
        ]);
        this.confirmPassword = new FormControl('', [Validators.required, Validators.minLength(8)]);
    }

    initFormGroup() {
        this.loginForm = new FormGroup({
            email: this.email,
            password: this.password,
        });
        this.forgotPasswordForm = new FormGroup({
            email: this.forgotPasswordEmailControl,
        });
        this.otpForm = new FormGroup({
            otp: this.otpCode,
        });
        this.resetPasswordForm = new FormGroup(
            {
                newPassword: this.newPassword,
                confirmPassword: this.confirmPassword,
            },
            { validators: this.passwordMatchValidator.bind(this) }
        );
    }

    passwordStrengthValidator(control: AbstractControl): { [key: string]: boolean } | null {
        const value = control.value;
        if (!value) {
            return null;
        }

        const hasCapitalLetter = /[A-Z]/.test(value);
        const hasNumber = /[0-9]/.test(value);
        const hasSymbol = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value);

        const isValid = hasCapitalLetter && hasNumber && hasSymbol;

        if (!isValid) {
            return {
                passwordStrength: true,
            };
        }
        return null;
    }

    passwordMatchValidator(group: AbstractControl) {
        const newPass = group.get('newPassword')?.value;
        const confirmPass = group.get('confirmPassword')?.value;
        return newPass === confirmPass ? null : { passwordMismatch: true };
    }

    hasCapitalLetter(): boolean {
        return /[A-Z]/.test(this.newPassword.value);
    }

    hasNumber(): boolean {
        return /[0-9]/.test(this.newPassword.value);
    }

    hasSymbol(): boolean {
        return /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(this.newPassword.value);
    }

    submit() {
        if (this.loginForm.valid) {
            const data: ILogin = {
                email: this.loginForm.value.email,
                password: this.loginForm.value.password,
            };
            this.loginAPI(data);
        } else {
            this.loginForm.markAllAsTouched();
            Object.keys(this.loginForm.controls).forEach((field) => {
                const control = this.loginForm.get(field);
                control?.markAsDirty();
            });
        }
    }

    openForgotPasswordForm() {
        this.currentStep = 'forgot-password-email';
        this.forgotPasswordForm.reset();
    }
    submitForgotPasswordEmail() {
        if (this.forgotPasswordForm.valid) {
            this.forgotPasswordEmail = this.forgotPasswordForm.value.email;
            this._NgxSpinnerService.show();

            this._userService.forgotPassword({ email: this.forgotPasswordEmail }).subscribe({
                next: (res) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        this.currentStep = 'otp';
                        this.show('success', 'Success', 'OTP sent to your email');
                        this.startResendTimer();
                    } else {
                        this.show('error', 'Error', res.cMessage || 'Failed to send OTP');
                    }
                },
                error: (err) => {
                    this._NgxSpinnerService.hide();
                    const errorMessage = err.error?.cMessage || 'An error occurred';
                    this.show('error', 'Error', errorMessage);
                },
            });
        } else {
            this.forgotPasswordForm.markAllAsTouched();
            this.show('error', 'Error', 'Please enter a valid email');
        }
    }

    backFromForgotPasswordEmail() {
        this.currentStep = 'login';
        this.forgotPasswordForm.reset();
    }

    verifyOTP() {
        if (this.otpForm.valid) {
            this._NgxSpinnerService.show();

            const data = {
                email: this.forgotPasswordEmail,
                otp: this.otpForm.value.otp,
            };

            this._userService.verifyOTP(data).subscribe({
                next: (res: any) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        // Store resetToken in localStorage
                        if (res.data?.resetToken) {
                            this.resetToken = res.data.resetToken;
                            localStorage.setItem('resetToken', this.resetToken);
                        }
                        this.currentStep = 'reset';
                        this.show('success', 'Success', 'OTP verified successfully');
                    } else {
                        this.show('error', 'Error', res.cMessage || 'Invalid OTP');
                    }
                },
                error: (err: any) => {
                    this._NgxSpinnerService.hide();
                    const errorMessage = err.error?.cMessage || 'An error occurred';
                    this.show('error', 'Error', errorMessage);
                },
            });
        } else {
            this.otpForm.markAllAsTouched();
            this.show('error', 'Error', 'Please enter a valid OTP');
        }
    }

    resetPassword() {
        if (this.resetPasswordForm.valid) {
            this._NgxSpinnerService.show();

            const data = {
                newPassword: this.resetPasswordForm.value.newPassword,
                confirmPassword: this.resetPasswordForm.value.confirmPassword,
            };

            this._userService.resetPassword(data).subscribe({
                next: (res: any) => {
                    this._NgxSpinnerService.hide();
                    if (res.status === 200) {
                        this.show('success', 'Success', 'Password reset successfully');
                        // Delete resetToken from localStorage
                        localStorage.removeItem('resetToken');
                        this.resetToken = '';
                        setTimeout(() => {
                            this.backToLogin();
                        }, 1500);
                    } else {
                        this.show('error', 'Error', res.cMessage || 'Failed to reset password');
                    }
                },
                error: (err: any) => {
                    this._NgxSpinnerService.hide();
                    const errorMessage = err.error?.cMessage || 'An error occurred';
                    this.show('error', 'Error', errorMessage);
                },
            });
        } else {
            this.resetPasswordForm.markAllAsTouched();
            this.show('error', 'Error', 'Passwords do not match or are invalid');
        }
    }

    backToLogin() {
        this.currentStep = 'login';
        this.resetPasswordForm.reset();
        this.otpForm.reset();
        this.email.reset();
        this.password.reset();
        this.resendTimer = 0;
        this.resetToken = '';
        localStorage.removeItem('resetToken');
    }

    resendOTP() {
        if (!this.resendDisabled) {
            this.submitForgotPasswordEmail();
        }
    }

    private startResendTimer() {
        this.resendDisabled = true;
        this.resendTimer = 60;

        const interval = setInterval(() => {
            this.resendTimer--;
            if (this.resendTimer <= 0) {
                clearInterval(interval);
                this.resendDisabled = false;
            }
        }, 1000);
    }

    loginAPI(data: ILogin): void {
        this._NgxSpinnerService.show();

        this._userService.login(data).subscribe({
            next: (res) => {
                this._NgxSpinnerService.hide();

                if (res.status === 200 && res.data && res.data.accessToken) {
                    this.show('success', 'Success', res.cMessage || 'Login successful');

                    localStorage.setItem('token', res.data.accessToken);
                    this._userService.setUser(res.data.user);
                    this.loadDepartments();
                    this.loadEmployeeDetails();
                    setTimeout(() => {
                        this._router.navigate(['/home']);
                    }, 500);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Login failed');
                    console.warn('Login failed (API reported error):', res);
                }
            },
            error: (err) => {
                this._NgxSpinnerService.hide();

                const errorMessage =
                    err.error?.cMessage ||
                    err.error?.sMessage ||
                    'An unexpected server error occurred';
                this.show('error', 'Error', errorMessage);
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

    viewOpenPositions(): void {
        this._router.navigate(['/job-portal']);
    }

    loadDepartments(): void {
        this._departmentService.getDepartments().subscribe({
            next: (res) => {
                if (res.status === 200 && res.data) {
                    this._departmentService.setDepartments(res.data);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load departments');
                }
            },
            error: (err) => {
                this.show(
                    'error',
                    'Error',
                    err?.error?.cMessage || 'An error occurred while loading departments'
                );
            },
        });
    }

    loadEmployeeDetails(): void {
        const user = this._userService.getUser();
        if (!user || !user.id) {
            console.error('User has no user ID');
            return;
        }

        this._employeeService.getEmployeeByUserId(user.id).subscribe({
            next: (res) => {
                if (res.status === 200) {
                    user.employeeId = res.data.employeeId;
                    user.departmentName = res.data.departmentName;
                    user.position = res.data.positionTitle;
                    user.departmentId = Number(
                        this._departmentService.getDepartmentIdByName(res.data.departmentName)
                    );
                    user.salaryNet = res.data.salaryNet;
                    user.salaryGross = res.data.salaryGross;
                    this._userService.setUser(user);
                    console.log('Updated user with employee details:', user);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employee details');
                }
            },
            error: (err) => {
                this.show(
                    'error',
                    'Error',
                    err?.error?.cMessage || 'An error occurred while loading employee details'
                );
            },
        });
    }
}
