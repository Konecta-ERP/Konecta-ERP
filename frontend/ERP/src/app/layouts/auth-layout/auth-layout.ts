import { Component } from '@angular/core';
import { AuthFooter } from '../../components/auth-footer/auth-footer';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-auth-layout',
    imports: [AuthFooter, RouterOutlet],
    templateUrl: './auth-layout.html',
    styleUrl: './auth-layout.css',
})
export class AuthLayout {}
