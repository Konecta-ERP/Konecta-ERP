import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
export const authGuard: CanActivateFn = (route, state) => {
    return true
    const router = inject(Router)
    const auth = inject(AuthService)
    if(auth.authorized()){
        return true;
    }
    router.navigate(['/auth']);
    return false;
};
