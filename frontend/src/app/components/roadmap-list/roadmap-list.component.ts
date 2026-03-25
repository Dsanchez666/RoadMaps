import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ConnectionStateService } from '../../services/connection-state.service';
import { Roadmap, RoadmapService } from '../../services/roadmap.service';

/**
 * RoadmapListComponent
 *
 * Shows available roadmaps with search and navigation actions.
 */
@Component({
  selector: 'app-roadmap-list',
  standalone: false,
  templateUrl: './roadmap-list.component.html',
  styleUrls: ['./roadmap-list.component.scss']
})
export class RoadmapListComponent implements OnInit, OnDestroy {
  roadmaps: Roadmap[] = [];
  filteredRoadmaps: Roadmap[] = [];
  loading = false;
  search = '';
  connectionError = '';
  reconnecting = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly service: RoadmapService,
    private readonly connectionState: ConnectionStateService
  ) {}

  /**
   * Initializes component subscriptions and first load.
   */
  ngOnInit(): void {
    this.service.roadmapsChanged$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.load());

    this.load();
  }

  /**
   * Cleans reactive subscriptions.
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Requests backend status and roadmap list.
   */
  load(): void {
    this.loading = true;
    this.connectionError = '';

    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
    this.service.list().subscribe({
      next: (r) => {
        this.roadmaps = r;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.connectionError = err?.error?.message || 'Error cargando roadmaps desde backend.';
        this.loading = false;
      }
    });
  }

  /**
   * Updates visible rows based on search criteria.
   */
  applyFilter(): void {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      this.filteredRoadmaps = [...this.roadmaps];
      return;
    }
    this.filteredRoadmaps = this.roadmaps.filter((r) =>
      `${r.title || ''} ${r.description || ''}`.toLowerCase().includes(term)
    );
  }

  /**
   * Navigates to roadmap viewer.
   */
  ver(roadmap: Roadmap): void {
    if (!roadmap.id) {
      return;
    }
    this.router.navigate(['/roadmaps', roadmap.id, 'view']);
  }

  /**
   * Navigates to roadmap editor.
   */
  editar(roadmap: Roadmap): void {
    if (!roadmap.id) {
      return;
    }
    this.router.navigate(['/roadmaps', roadmap.id, 'edit']);
  }

  /**
   * Starts reconnect flow and reloads current screen on success.
   */
  reconnect(): void {
    this.reconnecting = true;
    this.connectionState.reconnect().subscribe({
      next: () => {
        this.reconnecting = false;
        this.load();
      },
      error: (err) => {
        this.reconnecting = false;
        this.connectionError = err?.message || 'No se pudo reconectar';
      }
    });
  }
}
