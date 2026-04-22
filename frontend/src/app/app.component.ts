import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { ConnectionStateService } from './services/connection-state.service';
import { DatabaseStatus } from './services/database.service';
import { AuthService } from './services/auth.service';

/**
 * AppComponent
 *
 * Root shell with global status header and route outlet.
 */
@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  status: DatabaseStatus | null = null;
  reconnecting = false;
  reconnectError = '';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly connectionState: ConnectionStateService,
    public readonly authService: AuthService
  ) {}

  /**
   * Subscribes to route changes to refresh backend connection status.
   */
  ngOnInit(): void {
    this.connectionState.status$
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => {
        this.status = status;
        this.checkInitialAuth();
      });

    // Subscribe to auth changes to handle token validation results
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.checkInitialAuth();
      });

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event: NavigationEnd) => {
        this.handleRouteChange(event.url);
        this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
      });

    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkInitialAuth(): void {
    const currentUrl = this.router.url;
    const hasStoredToken = !!localStorage.getItem('roadmaps_session_token');
    const isAuthenticated = this.authService.isAuthenticated;
    const hasDbConnection = this.status !== null;

    console.log('🔍 [APP-INIT] Verificando estado inicial...');
    console.log(`   📍 URL actual: ${currentUrl}`);
    console.log(`   🔑 ¿Token en localStorage? ${hasStoredToken}`);
    console.log(`   ✅ ¿Autenticado? ${isAuthenticated}`);
    console.log(`   🗄️  ¿Conexión BD? ${hasDbConnection}`);
    console.log(`   📊 Status BD:`, this.status);

    // Routes que NO necesitan validación de BD
    const publicRoutes = ['/login', '/first-password'];
    if (publicRoutes.some(route => currentUrl.startsWith(route))) {
      console.log(`   ✓ Ruta pública (${currentUrl}) - sin validación`);
      return;
    }

    // Ruta /database - permite entrar sin restricciones
    if (currentUrl.startsWith('/database')) {
      console.log('   ✓ En ruta /database - permitido');
      return;
    }

    console.log('');
    console.log('📌 PASO 1: Verificar conexión a BD (CRÍTICO - es lo PRIMERO)');
    
    // ⚠️ REGLA DE ORO: SIN CONEXIÓN BD → SIEMPRE A /database
    if (!hasDbConnection) {
      console.log('   ❌ NO hay conexión a BD');
      console.log('   ➡️  NAVEGANDO A: /database');
      console.log('   📝 Razón: CONEXIÓN BD ES LO PRIMERO, antes que autenticación');
      console.log('');
      this.router.navigate(['/database']);
      return;
    }

    console.log('   ✅ SÍ hay conexión a BD');
    console.log('');
    console.log('📌 PASO 2: Verificar autenticación');
    
    // Con conexión BD: verificar autenticación
    if (!isAuthenticated && !hasStoredToken) {
      console.log('   ❌ NO hay autenticación');
      console.log('   ➡️  NAVEGANDO A: /login');
      console.log('   📝 Razón: BD conectada, pero sin token - ir a login');
      console.log('');
      this.router.navigate(['/login']);
      return;
    }

    console.log('   ✅ SÍ está autenticado');
    console.log('');
    console.log('📌 PASO 3: Verificar rol');
    const currentUser = this.authService.currentUser;
    if (currentUser?.rol === 'ADMIN') {
      console.log(`   👑 ADMIN (${currentUser.username})`);
      console.log('   ℹ️  Se redirigirá a /admin/users');
    } else {
      console.log(`   📊 ${currentUser?.rol} (${currentUser?.username})`);
      console.log('   ℹ️  Se redirigirá a /roadmaps');
    }
    console.log('');
  }

  private handleRouteChange(url: string): void {
    // Routes that don't require database connection
    const noDbRoutes = ['/database', '/login', '/first-password'];
    if (noDbRoutes.some(route => url.startsWith(route))) {
      // For auth routes, allow without auth check
      if (url.startsWith('/login') || url.startsWith('/first-password')) {
        return;
      }
      // For database route, allow always
      return;
    }

    // For protected routes that require database connection
    // Check authentication first
    if (!this.authService.isAuthenticated) {
      const hasStoredToken = !!localStorage.getItem('roadmaps_session_token');
      if (hasStoredToken) {
        // Token exists but not validated yet
        return;
      }
      this.router.navigate(['/login']);
      return;
    }

    // User is authenticated - allow navigation
    // Components will handle database connection status and show error/reconnect UI if needed
  }

  /**
   * Returns whether current route is DB configuration screen.
   */
  isDatabaseRoute(): boolean {
    return this.router.url.startsWith('/database') || this.router.url === '/';
  }

  /**
   * Performs reconnect with cached credentials.
   */
  reconnect(): void {
    this.reconnecting = true;
    this.reconnectError = '';
    this.connectionState.reconnect().subscribe({
      next: () => {
        this.reconnecting = false;
      },
      error: (err) => {
        this.reconnecting = false;
        this.reconnectError = err?.message || 'No se pudo reconectar';
      }
    });
  }

  /**
   * Logs out the current user and navigates to login.
   */
  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
