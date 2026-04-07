import { Component } from '@angular/core';
import { from } from 'rxjs';
import { concatMap, finalize } from 'rxjs/operators';

import { Roadmap, RoadmapImportPayload, RoadmapService } from '../../services/roadmap.service';

/**
 * Component RoadmapCreateComponent
 *
 * Handles manual creation and JSON import with explicit save/discard decision.
 */
@Component({
  selector: 'app-roadmap-create',
  standalone: false,
  templateUrl: './roadmap-create.component.html',
  styleUrls: ['./roadmap-create.component.scss']
})
export class RoadmapCreateComponent {
  title = '';
  description = '';
  saving = false;

  importing = false;
  importError = '';
  importedCount = 0;
  preview: RoadmapImportPayload | null = null;
  pendingImports: RoadmapImportPayload[] = [];

  constructor(private readonly service: RoadmapService) {}

  /**
   * Creates one roadmap from manual form data.
   */
  save(): void {
    if (!this.title.trim()) {
      return;
    }

    this.saving = true;
    const payload: Roadmap = {
      title: this.title.trim(),
      description: this.description.trim()
    };

    this.service.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.title = '';
        this.description = '';
        this.service.notifyRoadmapsChanged();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  /**
   * Parses selected JSON and stores entries as pending (not persisted yet).
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) {
      return;
    }

    this.importError = '';
    this.importedCount = 0;
    this.preview = null;
    this.pendingImports = [];

    const reader = new FileReader();
    reader.onload = () => {
      try {
        const raw = JSON.parse(String(reader.result || ''));
        const items = this.extractItems(raw);
        if (!items || items.length === 0) {
          this.importError = 'No hay roadmaps válidos en el JSON.';
          return;
        }

        const unique = new Set<string>();
        this.pendingImports = items
          .map((item) => this.normalizeImportPayload(item))
          .filter((item) => {
            const key = `${item.title || item.producto || ''}::${item.description || ''}`;
            if (!key.trim()) {
              return false;
            }
            if (unique.has(key)) {
              return false;
            }
            unique.add(key);
            return true;
          });

        if (this.pendingImports.length === 0) {
          this.importError = 'No hay roadmaps válidos en el JSON.';
          return;
        }

        this.preview = this.pendingImports[0];
      } catch {
        this.importError = 'No se pudo leer el JSON.';
      } finally {
        input.value = '';
      }
    };
    reader.onerror = () => {
      this.importError = 'No se pudo leer el archivo.';
      input.value = '';
    };
    reader.readAsText(file);
  }

  /**
   * Persists all pending import items in backend storage.
   */
  guardarImportacion(): void {
    if (this.pendingImports.length === 0) {
      return;
    }

    this.importing = true;
    this.importError = '';
    this.importedCount = 0;

    from(this.pendingImports)
      .pipe(
        concatMap((payload) => this.service.importRoadmap(payload)),
        finalize(() => {
          this.importing = false;
        })
      )
      .subscribe({
        next: () => {
          this.importedCount += 1;
        },
        error: () => {
          this.importError = 'Error importando roadmaps en base de datos.';
        },
        complete: () => {
          this.pendingImports = [];
          this.preview = null;
          this.service.notifyRoadmapsChanged();
        }
      });
  }

  /**
   * Discards pending import preview and refreshes list from backend.
   */
  descartarImportacion(): void {
    this.pendingImports = [];
    this.preview = null;
    this.importError = '';
    this.importedCount = 0;
    this.service.notifyRoadmapsChanged();
  }

  private extractItems(raw: any): any[] {
    if (Array.isArray(raw?.roadmaps)) {
      return raw.roadmaps;
    }
    if (Array.isArray(raw)) {
      return raw;
    }
    if (raw && typeof raw === 'object') {
      return [raw];
    }
    return [];
  }

  private normalizeImportPayload(raw: any): RoadmapImportPayload {
    return {
      title: String(raw?.title || raw?.producto || '').trim(),
      description: String(raw?.description || '').trim(),
      producto: String(raw?.producto || raw?.title || '').trim(),
      organizacion: String(raw?.organizacion || '').trim(),
      horizonte_base: {
        inicio: String(raw?.horizonte_base?.inicio || ''),
        fin: String(raw?.horizonte_base?.fin || '')
      },
      ejes_estrategicos: Array.isArray(raw?.ejes_estrategicos) ? raw.ejes_estrategicos : [],
      iniciativas: Array.isArray(raw?.iniciativas) ? raw.iniciativas : [],
      compromisos: Array.isArray(raw?.compromisos) ? raw.compromisos : []
    };
  }
}
