import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Vérifier si on est dans un environnement navigateur
  if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('token');

    if (token) {
      // Vérifier si le token est expiré
      if (isTokenExpired(token)) {
        handleTokenExpiration(router);
        return next(req);
      }

      // Cloner la requête avec le header Authorization
      const cloned = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });

      return next(cloned).pipe(
          catchError(error => {
            if (error.status === 401) {
              handleTokenExpiration(router);
            }
            return throwError(() => error);
          })
      );
    }
  }

  return next(req);
};

// Vérifier si le token est expiré
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000;
    return Date.now() >= exp;
  } catch {
    return true;
  }
}

// Gérer l'expiration du token
function handleTokenExpiration(router: Router): void {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('user');
  router.navigate(['/login']);
}