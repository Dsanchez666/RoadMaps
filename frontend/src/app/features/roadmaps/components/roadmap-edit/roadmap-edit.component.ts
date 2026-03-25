import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoadmapService } from '../../services/roadmap.service';
import { Roadmap } from '../../../../shared/models/roadmap.model';
import { Axis, Initiative, InitiativeDependency, RoadmapConfig } from '../../../../shared/models/roadmap-config.model';
import { HeaderActionsService } from '../../../../shared/services/header-actions.service';
import { DatabaseService } from '../../../database/services/database.service';

@Component({
  selector: 'app-roadmap-edit',
  standalone: false,
  templateUrl: './roadmap-edit.component.html',
  styleUrls: ['./roadmap-edit.component.scss']
})
export class RoadmapEditComponent implements OnInit, OnDestroy {
  roadmap: Roadmap | null = null;
  loading = true;
  config: RoadmapConfig | null = null;
  message = '';
  messageType: 'success' | 'error' | '' = '';
  preview = true;
  screenError = '';
  private currentId = '';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private service: RoadmapService,
    private headerActions: HeaderActionsService,
    private databaseService: DatabaseService
  ) {}

  ngOnInit(): void {
    this.databaseService.status$
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => {
        if (status?.connected && this.currentId && this.screenError) {
          this.loadRoadmap(this.currentId);
        }
      });

    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = params.get('id');
        this.screenError = '';
        this.loading = true;

        if (!id) {
          this.loading = false;
          this.screenError = 'No se recibio el identificador del roadmap.';
          return;
        }

        this.currentId = id;
        this.setHeaderActions(id);
        this.loadRoadmap(id);
      });
  }

  ngOnDestroy(): void {
    this.headerActions.clear();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setHeaderActions(id: string) {
    this.headerActions.setActions([
      { label: 'Volver', route: '/roadmaps' },
      { label: 'Ver', route: ['/roadmaps', id, 'view'], variant: 'primary' }
    ]);
  }

  onTogglePreview() {
    this.preview = !this.preview;
  }

  onSave() {
    if (!this.config || !this.roadmap) return;
    this.saveConfig(this.roadmap.id as string, this.config);
    this.messageType = 'success';
    this.message = 'Configuracion guardada en el navegador';
  }

  onDiscard() {
    if (!this.roadmap) return;
    this.loadConfig(this.roadmap.id as string);
    this.messageType = 'success';
    this.message = 'Cambios descartados';
  }

  addAxis() {
    if (!this.config) return;
    const axis: Axis = {
      id: '',
      nombre: '',
      descripcion: '',
      color: '#1976D2'
    };
    this.config.ejes_estrategicos.push(axis);
  }

  removeAxis(index: number) {
    if (!this.config) return;
    this.config.ejes_estrategicos.splice(index, 1);
  }

  addInitiative() {
    if (!this.config) return;
    const initiative: Initiative = {
      id: '',
      nombre: '',
      eje: '',
      tipo: '',
      expediente: '',
      inicio: '',
      fin: '',
      objetivo: '',
      impacto_principal: '',
      usuarios_afectados: '',
      dependencias: [],
      certeza: ''
    };
    this.config.iniciativas.push(initiative);
  }

  removeInitiative(index: number) {
    if (!this.config) return;
    this.config.iniciativas.splice(index, 1);
  }

  addDependency(initiative: Initiative) {
    const dep: InitiativeDependency = { iniciativa: '', tipo: '' };
    initiative.dependencias = initiative.dependencias || [];
    initiative.dependencias.push(dep);
  }

  removeDependency(initiative: Initiative, index: number) {
    initiative.dependencias.splice(index, 1);
  }

  private loadRoadmap(id: string): void {
    this.loading = true;
    this.screenError = '';

    this.service.get(id).subscribe({
      next: (r) => {
        this.roadmap = r;
        this.loadConfig(id);
      },
      error: (err) => {
        this.roadmap = null;
        this.loading = false;
        this.screenError = err?.error?.message || 'No se pudo cargar el roadmap (conexion o servidor).';
      }
    });
  }

  private loadConfig(id: string) {
    const stored = this.loadLocalConfig(id);
    if (stored) {
      this.config = this.ensureConfigDefaults(JSON.parse(JSON.stringify(stored)));
      this.loading = false;
      return;
    }
    this.service.getConfig(id).subscribe({
      next: (cfg) => {
        this.config = this.ensureConfigDefaults(JSON.parse(JSON.stringify(cfg)));
        this.saveConfig(id, cfg);
        this.loading = false;
      },
      error: (err) => {
        this.config = null;
        this.loading = false;
        this.screenError = err?.error?.message || 'No se pudo cargar la configuracion del roadmap.';
      }
    });
  }

  private loadLocalConfig(id: string): RoadmapConfig | null {
    const raw = localStorage.getItem(this.storageKey(id));
    if (!raw) {
      return null;
    }
    try {
      const cfg = JSON.parse(raw) as RoadmapConfig;
      if (cfg && cfg.ejes_estrategicos && cfg.ejes_estrategicos.length) {
        return cfg;
      }
      return null;
    } catch {
      return null;
    }
  }

  private saveConfig(id: string, cfg: RoadmapConfig) {
    localStorage.setItem(this.storageKey(id), JSON.stringify(cfg));
  }

  private storageKey(id: string): string {
    return `roadmap_config_${id}`;
  }

  private ensureConfigDefaults(cfg: RoadmapConfig): RoadmapConfig {
    if (!cfg.horizonte_base) {
      cfg.horizonte_base = { inicio: '', fin: '' };
    }
    cfg.ejes_estrategicos = cfg.ejes_estrategicos || [];
    cfg.iniciativas = cfg.iniciativas || [];
    cfg.iniciativas.forEach((initiative) => {
      initiative.dependencias = initiative.dependencias || [];
    });
    return cfg;
  }
}