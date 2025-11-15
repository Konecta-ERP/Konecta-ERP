
import { Component } from '@angular/core';
import {
    FormControl,
    FormGroup,
    Validators
} from '@angular/forms';
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
  imports: [
    SharedModule
  ],
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
        this.initFormControls()
        this.initFormGroup()
    }
    email!: FormControl
    password!: FormControl
    loginForm!: FormGroup
    showPassword = false;

    togglePassword() {
    this.showPassword = !this.showPassword;
    }
    initFormControls() {
        this.email = new FormControl('',[Validators.required, Validators.email])
        this.password = new FormControl('',[Validators.required])
    }

    initFormGroup() {
        this.loginForm = new FormGroup({
            email: this.email,
            password: this.password
        })
    }

    submit() {
        if (this.loginForm.valid) {
            const data: ILogin = {
                email: this.loginForm.value.email,
                password: this.loginForm.value.password
            }
            this.loginAPI(data)
        }else{
            this.loginForm.markAllAsTouched()
            Object.keys(this.loginForm.controls).forEach(field => {
                const control = this.loginForm.get(field);
                control?.markAsDirty()
            });
        }

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

            const errorMessage = err.error?.cMessage || err.error?.sMessage || 'An unexpected server error occurred';
            this.show('error', 'Error', errorMessage);
        }
    });
    }

    show(severity: string='info', summary: string='Info', detail: string='Message Content'): void {
        this._messageService.add({ severity, summary, detail, life: 3000 });
    }

    viewOpenPositions(): void {
        this._router.navigate(['/open-positions']);
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
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading departments');
            }
        });
    }

    loadEmployeeDetails(): void {
        const user = this._userService.getUser();
        if (!user || !user.id) {
            console.error("User has no user ID");
            return;
        }

        this._employeeService.getEmployeeByUserId(user.id).subscribe({
            next: (res) => {
                if (res.status === 200) {
                    user.employeeId = res.data.employeeId;
                    user.departmentName= res.data.departmentName;
                    user.position= res.data.positionTitle;
                    user.departmentId = Number(this._departmentService.getDepartmentIdByName(res.data.departmentName))
                    user.salaryNet = res.data.salaryNet;
                    user.salaryGross = res.data.salaryGross;
                    this._userService.setUser(user);
                    console.log("Updated user with employee details:", user);
                } else {
                    this.show('error', 'Error', res.cMessage || 'Failed to load employee details');
                }
            },
            error: (err) => {
                this.show('error', 'Error', err?.error?.cMessage || 'An error occurred while loading employee details');
            }
        });
    }


}
