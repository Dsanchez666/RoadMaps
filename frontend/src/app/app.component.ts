import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { DatabaseService } from './features/database/services/database.service';
import { DatabaseStatus } from './shared/models/database.model';
import { HeaderAction } from './shared/models/header-action.model';
import { HeaderActionsService } from './shared/services/header-actions.service';

@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  connected = false;
  status: DatabaseStatus | null = null;
  headerActions: HeaderAction[] = [];
  reconnectMessage = '';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private databaseService: DatabaseService,
    private headerActionsService: HeaderActionsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.databaseService.status$
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => {
        this.status = status;
        this.connected = Boolean(status?.connected);
      });

    this.headerActionsService.actions$
      .pipe(takeUntil(this.destroy$))
      .subscribe((actions) => { this.headerActions = actions; });

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.refreshStatus();
      });

    this.refreshStatus();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  reconnect(): void {
    this.reconnectMessage = '';
    this.databaseService.reconnectLast().subscribe({
      next: (status) => {
        this.status = status;
        this.connected = Boolean(status?.connected);
        this.reconnectMessage = this.connected ? '' : 'No se pudo recuperar la conexion.';
      },
      error: (err) => {
        this.connected = false;
        this.reconnectMessage = err?.message || 'No se pudo reconectar con la ultima configuracion.';
      }
    });
  }

  disconnect(): void {
    if (!this.connected) {
      this.router.navigateByUrl('/roadmaps');
      return;
    }

    this.databaseService.disconnect().subscribe({
      next: () => {
        this.connected = false;
        this.router.navigateByUrl('/roadmaps');
      },
      error: () => {
        this.connected = false;
      }
    });
  }

  private refreshStatus() {
    this.databaseService.getStatus().subscribe({
      next: (status) => {
        this.status = status;
        this.connected = Boolean(status?.connected);
      },
      error: () => {
        this.status = null;
        this.connected = false;
      }
    });
  }
}