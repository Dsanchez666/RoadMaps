import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoadmapService } from '../../services/roadmap.service';
import { Roadmap } from '../../../../shared/models/roadmap.model';
import { RoadmapConfig, Axis, Initiative, InitiativeDependency } from '../../../../shared/models/roadmap-config.model';

@Component({
  selector: 'app-roadmap-edit',
  standalone: false,
  templateUrl: './roadmap-edit.component.html',
  styleUrls: ['./roadmap-edit.component.scss']
})
export class RoadmapEditComponent implements OnInit, OnDestroy {
  roadmap: Roadmap | null = null;
  loading = true;
  state: RoadmapConfig = this.emptyState();
  jsonOutput = '';
  isDirty = false;
  savedState: RoadmapConfig | null = null;
  currentJsonName = 'roadmap_etna.json';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: RoadmapService
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = params.get('id');
        if (!id) {
          this.router.navigateByUrl('/');
          return;
        }
        this.loading = true;
        this.service.get(id).subscribe({
          next: (r) => {
            this.roadmap = r;
            this.loadConfig(id, r.title || '');
          },
          error: () => { this.roadmap = null; this.loading = false; }
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onBaseChange() {
    this.markDirty();
    this.updateJson();
  }

  addAxis() {
    this.state.ejes_estrategicos.push({ id: '', nombre: '', descripcion: '', color: '' } as Axis);
    this.markDirty();
    this.updateJson();
  }

  removeAxis(index: number) {
    this.state.ejes_estrategicos.splice(index, 1);
    this.reconcileInitiativeAxes(true);
    this.markDirty();
    this.updateJson();
  }

  addInitiative() {
    this.state.iniciativas.push({
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
    } as Initiative);
    this.markDirty();
    this.updateJson();
  }

  removeInitiative(index: number) {
    this.state.iniciativas.splice(index, 1);
    this.markDirty();
    this.updateJson();
  }

  addDependency(initIndex: number) {
    const deps = this.state.iniciativas[initIndex].dependencias || [];
    deps.push({ iniciativa: '', tipo: '' } as InitiativeDependency);
    this.state.iniciativas[initIndex].dependencias = deps;
    this.markDirty();
    this.updateJson();
  }

  removeDependency(initIndex: number, depIndex: number) {
    this.state.iniciativas[initIndex].dependencias.splice(depIndex, 1);
    this.markDirty();
    this.updateJson();
  }

  loadJson(file?: File | null) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const parsed = JSON.parse(String(reader.result || '{}')) as RoadmapConfig;
        this.state = parsed;
        this.currentJsonName = file.name || 'roadmap_etna.json';
        this.savedState = JSON.parse(JSON.stringify(this.state));
        this.isDirty = false;
        this.updateJson();
      } catch {
        alert('JSON no valido.');
      }
    };
    reader.readAsText(file);
  }

  saveChanges() {
    if (!this.roadmap) return;
    this.reconcileInitiativeAxes(true);
    this.savedState = JSON.parse(JSON.stringify(this.state));
    this.updateJson();
    this.saveConfig(this.roadmap.id as string, this.state);
    this.downloadJson();
    this.isDirty = false;
  }

  discardChanges() {
    if (!this.savedState) return;
    this.state = JSON.parse(JSON.stringify(this.savedState));
    this.updateJson();
    this.isDirty = false;
  }

  downloadJson() {
    const blob = new Blob([this.jsonOutput], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.currentJsonName || 'roadmap_etna.json';
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      URL.revokeObjectURL(url);
      a.remove();
    }, 0);
  }

  markDirty() {
    this.isDirty = true;
  }

  updateJson() {
    this.jsonOutput = JSON.stringify(this.state, null, 2);
  }

  normalizeAxisId(value: string): string {
    if (!value) return '';
    let v = String(value).toUpperCase().trim();
    v = v.replace(/[^A-Z0-9]/g, '');
    if (v.startsWith('EJE')) {
      v = 'E' + v.slice(3);
    }
    return v;
  }

  reconcileInitiativeAxes(notify: boolean) {
    const axisMap = new Map<string, string>();
    this.state.ejes_estrategicos.forEach(a => {
      const norm = this.normalizeAxisId(a.id);
      if (norm) axisMap.set(norm, a.id);
    });

    let fixed = 0;
    let cleared = 0;

    this.state.iniciativas.forEach(init => {
      if (!init.eje) return;
      if (this.state.ejes_estrategicos.some(a => a.id === init.eje)) return;
      const norm = this.normalizeAxisId(init.eje);
      if (axisMap.has(norm)) {
        init.eje = axisMap.get(norm) as string;
        fixed += 1;
      } else {
        init.eje = '';
        cleared += 1;
      }
    });

    if (notify && (fixed || cleared)) {
      let msg = 'Se han actualizado referencias de ejes en iniciativas.';
      if (cleared) {
        msg += ' Algunas iniciativas quedaron sin eje porque el eje ya no existe.';
      }
      alert(msg);
    }
  }

  private loadConfig(id: string, title: string) {
    const stored = this.loadLocalConfig(id);
    if (stored) {
      this.state = stored;
      this.savedState = JSON.parse(JSON.stringify(this.state));
      this.updateJson();
      this.isDirty = false;
      this.loading = false;
      return;
    }

    this.service.getConfig(id).subscribe({
      next: (cfg) => {
        this.state = cfg;
        this.savedState = JSON.parse(JSON.stringify(this.state));
        this.updateJson();
        this.isDirty = false;
        this.saveConfig(id, cfg);
        this.loading = false;
      },
      error: () => {
        this.state = this.emptyState(title);
        this.savedState = JSON.parse(JSON.stringify(this.state));
        this.updateJson();
        this.isDirty = false;
        this.loading = false;
      }
    });
  }

  private loadLocalConfig(id: string): RoadmapConfig | null {
    const raw = localStorage.getItem(this.storageKey(id));
    if (!raw) return null;
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

  private emptyState(title?: string): RoadmapConfig {
    return {
      producto: title || '',
      organizacion: '',
      horizonte_base: { inicio: '', fin: '' },
      ejes_estrategicos: [],
      iniciativas: []
    };
  }
}
