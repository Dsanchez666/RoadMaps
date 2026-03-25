import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { ConnectionStateService } from './services/connection-state.service';
import { DatabaseStatus } from './services/database.service';

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
    private readonly connectionState: ConnectionStateService
  ) {}

  /**
   * Subscribes to route changes to refresh backend connection status.
   */
  ngOnInit(): void {
    this.connectionState.status$
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => (this.status = status));

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
      });

    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
  }

  /**
   * Releases subscriptions owned by the component.
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
   * Disconnects and navigates to connection setup screen.
   */
  salir(): void {
    this.connectionState.disconnect().subscribe({
      next: () => this.router.navigate(['/database']),
      error: () => this.router.navigate(['/database'])
    });
  }
}
