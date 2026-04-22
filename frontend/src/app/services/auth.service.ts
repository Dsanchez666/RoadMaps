import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface AuthUser {
  usuarioId: number;
  username: string;
  rol: 'ADMIN' | 'GESTION' | 'CONSULTA';
  mustChangePassword: boolean;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  usuarioId: number;
  username: string;
  rol: 'ADMIN' | 'GESTION' | 'CONSULTA';
  mustChangePassword: boolean;
}

export interface FirstPasswordPayload {
  username: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenStorageKey = 'roadmaps_session_token';
  private readonly userStorageKey = 'roadmaps_current_user';
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();
  private tokenValidated = false;

  constructor(private readonly http: HttpClient) {
    this.loadFromStorage();
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  get currentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  get isAuthenticated(): boolean {
    return this.tokenValidated && !!this.token && !!this.currentUser;
  }

  login(payload: LoginPayload): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    }).pipe(
      tap((response) => {
        this.storeToken(response.token);
        this.storeCurrentUser({
          usuarioId: response.usuarioId,
          username: response.username,
          rol: response.rol,
          mustChangePassword: response.mustChangePassword
        });
        this.tokenValidated = true;
      })
    );
  }

  setFirstPassword(payload: FirstPasswordPayload): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/first-password', payload).pipe(
      tap((response) => {
        this.storeToken(response.token);
        this.storeCurrentUser({
          usuarioId: response.usuarioId,
          username: response.username,
          rol: response.rol,
          mustChangePassword: response.mustChangePassword
        });
        this.tokenValidated = true;
      })
    );
  }

  logout(): Observable<any> {
    const token = this.token;
    if (!token) {
      this.clearSession();
      return of(null);
    }
    return this.http.post('/api/auth/logout', {}).pipe(
      tap(() => this.clearSession())
    );
  }

  refreshCurrentUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>('/api/auth/me').pipe(
      tap((user) => {
        this.storeCurrentUser(user);
      })
    );
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
  }

  private storeCurrentUser(user: AuthUser): void {
    this.currentUserSubject.next(user);
    localStorage.setItem(this.userStorageKey, JSON.stringify(user));
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.userStorageKey);
    this.currentUserSubject.next(null);
    this.tokenValidated = false;
  }

  private loadFromStorage(): void {
    const token = this.token;
    const storedUser = localStorage.getItem(this.userStorageKey);

    if (!token || !storedUser) {
      this.currentUserSubject.next(null);
      this.tokenValidated = true;
      return;
    }

    try {
      const user = JSON.parse(storedUser) as AuthUser;
      this.currentUserSubject.next(user);
      this.tokenValidated = true;
      
      // Validate token with backend only after a small delay to ensure HTTP is ready
      setTimeout(() => {
        this.validateToken().subscribe({
          next: (isValid) => {
            if (!isValid) {
              console.warn('Token validation failed on app init');
              this.clearSession();
            }
          },
          error: (err) => {
            console.warn('Token validation error on app init:', err);
            // Don't clear on error - the token might be valid but the call failed
            // The interceptor will handle 401 errors
          }
        });
      }, 100);
    } catch {
      this.tokenValidated = true;
      this.clearSession();
    }
  }

  private validateToken(): Observable<boolean> {
    return this.http.get('/api/auth/me').pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }
}
