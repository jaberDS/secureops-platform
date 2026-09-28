import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  let authenticatedRequest = req;

  if (token) {
    authenticatedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authenticatedRequest).pipe(

    catchError((error) => {

      if (error.status === 401) {

        console.warn(
          'AuthInterceptor: JWT expired or invalid. Redirecting to login.'
        );

        authService.logout();

        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};