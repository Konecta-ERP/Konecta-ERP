
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

    loginAPI(data:ILogin): void {
        this._NgxSpinnerService.show();
        this._authService.login(data).subscribe({
            next: (res) => {
                this.show('success', 'Success', 'Login successful' )
                console.log(res);
                this._NgxSpinnerService.hide();
                localStorage.setItem('token', res.token);
                this._router.navigate(['/home']);
            },
            error: (err) => {
                this.show('error', 'Error', err.error.error || 'Login failed' )
                this._NgxSpinnerService.hide();
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
