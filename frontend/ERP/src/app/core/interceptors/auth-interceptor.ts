import { HttpInterceptorFn } from '@angular/common/http';
export const authInterceptor: HttpInterceptorFn = (req, next) => {

    if (req.url.includes('/identity/auth/login')) {
        return next(req);
    }




  const token = localStorage.getItem('token');

  // Clone request and add authorization header if token exists
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
