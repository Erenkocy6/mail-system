import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth/auth-service';
import {tap} from 'rxjs';
import { API_BASE_URL } from '../constants';

export const authInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  if (!isBackendApiRequest(req.url)) {
    return next(req);
  }

  const authService = inject(AuthService);
  const authToken = authService.getAccessToken();

  if (!authToken) {
    return next(req);
  }

  const newReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${authToken}`),
  });


  return next(newReq).pipe(
    tap({
      error: (error: HttpErrorResponse) => {
        if(error.status === 401) {
          authService.logout();
        }
      }
    }),
  );
};

function isBackendApiRequest(url: string): boolean {
  const parsedUrl = new URL(url, window.location.origin);
  return parsedUrl.pathname === API_BASE_URL || parsedUrl.pathname.startsWith(`${API_BASE_URL}/`);
}
