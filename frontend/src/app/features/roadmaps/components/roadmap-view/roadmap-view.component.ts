import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoadmapService } from '../../services/roadmap.service';
import { Roadmap } from '../../../../shared/models/roadmap.model';
import { RoadmapConfig } from '../../../../shared/models/roadmap-config.model';

@Component({
  selector: 'app-roadmap-view',
  standalone: false,
  templateUrl: './roadmap-view.component.html',
  styleUrls: ['./roadmap-view.component.scss']
})
export class RoadmapViewComponent implements OnInit, OnDestroy {
  roadmap: Roadmap | null = null;
  loading = true;
  config: RoadmapConfig | null = null;
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
            this.loadConfig(id);
          },
          error: () => { this.roadmap = null; this.loading = false; }
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onConfigChange(cfg: RoadmapConfig) {
    if (!this.roadmap) return;
    this.saveConfig(this.roadmap.id as string, cfg);
    this.config = cfg;
  }

  private loadConfig(id: string) {
    const stored = this.loadLocalConfig(id);
    if (stored) {
      this.config = stored;
      this.loading = false;
      return;
    }
    this.service.getConfig(id).subscribe({
      next: (cfg) => {
        this.config = cfg;
        this.saveConfig(id, cfg);
        this.loading = false;
      },
      error: () => {
        this.config = null;
        this.loading = false;
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
}
