import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { AuthService } from '../../services/auth.service';
import { ConnectionStateService } from '../../services/connection-state.service';
import { Roadmap, RoadmapService } from '../../services/roadmap.service';

/**
 * RoadmapListComponent
 *
 * Shows available roadmaps with search and navigation actions.
 * Standalone component for routing.
 */
@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  selector: 'app-roadmap-list',
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
    private readonly connectionState: ConnectionStateService,
    public readonly authService: AuthService
  ) {}

  /**
   * Initializes component subscriptions and first load.
   */
  ngOnInit(): void {
    console.log('');
    console.log('📊 [ROADMAP-LIST] Componente inicializado');
    
    this.service.roadmapsChanged$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        console.log('🔄 Cambios en roadmaps detectados - recargando...');
        this.load();
      });

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

    console.log('📌 Cargando roadmaps...');
    
    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
    this.service.list().subscribe({
      next: (r) => {
        console.log(`   ✅ ${r.length} roadmaps cargados`);
        this.roadmaps = r;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.log('   ❌ Error cargando roadmaps');
        console.log(`   📝 ${err?.error?.message || err?.message}`);
        this.connectionError = err?.error?.message || 'Error cargando roadmaps desde backend.';
        this.loading = false;
        // If connection error, redirect to database config
        if (err.status === 0 || err.error?.message?.includes('connection')) {
          console.log('   ⚠️  Error de conexión detectado - redirigiendo a /database');
          setTimeout(() => this.router.navigate(['/database']), 1000);
        }
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
   * Redirects to database config for reconnection.
   */
  reconnect(): void {
    this.router.navigate(['/database']);
  }

  /**
   * Verifica si el usuario puede editar roadmaps.
   */
  canEdit(): boolean {
    const userRole = this.authService.currentUser?.rol;
    return userRole === 'ADMIN' || userRole === 'GESTION';
  }

  /**
   * Verifica si el usuario es solo consulta (lectura).
   */
  isConsulta(): boolean {
    return this.authService.currentUser?.rol === 'CONSULTA';
  }
}
