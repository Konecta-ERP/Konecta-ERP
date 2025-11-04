
import { Component } from '@angular/core';
import {
    FormControl,
    FormGroup,
    Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ILogin } from '../../core/interfaces/ilogin';
import { MessageService } from 'primeng/api';
import { NgxSpinnerService } from 'ngx-spinner';
import { SharedModule } from '../../shared/module/shared/shared-module';
import { UserService } from '../../core/services/user.service';
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
        private _authService: AuthService,
        private _messageService: MessageService,
        private _NgxSpinnerService: NgxSpinnerService,
        private _userService: UserService,
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

    this._authService.login(data).subscribe({
        next: (res) => {
            this._NgxSpinnerService.hide();

            if (res.status === 200 && res.data && res.data.accessToken) {
                this.show('success', 'Success', res.cMessage || 'Login successful');


                localStorage.setItem('token', res.data.accessToken);
                this._userService.setUser(res.data.user);
                console.log('Logged in user:', res.data.user);
                console.log(this._userService.getUser());
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


}
