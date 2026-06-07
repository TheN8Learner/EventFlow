import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const apiBaseUrl = environment.apiBaseUrl.replace(/\/$/, '');
  const isApiRequest = req.url.startsWith(apiBaseUrl) || req.url.startsWith('/api/');
  const isRefreshRequest = req.url.endsWith('/api/v1/refresh-token');

  if (!token || !isApiRequest || isRefreshRequest) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq).pipe(
    catchError((error) => {
      if (error?.status !== 401) {
        return throwError(() => error);
      }

      return auth.refreshSession().pipe(
        switchMap((tokens) => {
          if (!tokens) {
            return throwError(() => error);
          }

          return next(req.clone({
            setHeaders: {
              Authorization: `Bearer ${tokens.accessToken}`
            }
          }));
        })
      );
    })
  );
};
