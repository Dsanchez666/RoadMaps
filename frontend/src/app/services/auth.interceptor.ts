import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { ConnectionStateService } from './connection-state.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly connectionState: ConnectionStateService
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Don't add token for auth endpoints
    if (req.url.includes('/api/auth/login') || req.url.includes('/api/auth/first-password')) {
      return next.handle(req);
    }

    const token = this.authService.token;
    if (!token) {
      return next.handle(req);
    }

    // Clone the request and add authorization header
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized - token is invalid or expired
        if (error.status === 401) {
          console.warn('⚠️  Token inválido o expirado.');
          console.log('🔍 Verificando conexión a BD antes de redirigir...');
          
          // Check if there's database connection
          const dbStatus = this.connectionState.statusSnapshot;
          const hasDbConnection = dbStatus !== null && dbStatus.connected;
          console.log(`📊 Estado BD actual: ${hasDbConnection ? 'CONECTADO ✅' : 'DESCONECTADO ❌'}`);
          
          if (!hasDbConnection) {
            // No BD connection - go to database config
            console.log('➡️  Redirigiendo a /database para configurar conexión');
            this.authService.logout().subscribe({
              next: () => this.router.navigate(['/database']),
              error: () => this.router.navigate(['/database'])
            });
          } else {
            // BD connected - go to login
            console.log('➡️  Token expirado, redirigiendo a /login');
            this.authService.logout().subscribe({
              next: () => this.router.navigate(['/login']),
              error: () => this.router.navigate(['/login'])
            });
          }
        }
        return throwError(() => error);
      })
    );
  }
}
