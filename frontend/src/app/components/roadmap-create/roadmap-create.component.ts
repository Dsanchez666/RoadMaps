import { Component } from '@angular/core';
import { from } from 'rxjs';
import { concatMap, finalize } from 'rxjs/operators';
import { RoadmapService, Roadmap } from '../../services/roadmap.service';

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
  uploading = false;
  uploadError = '';
  uploaded = 0;
  preview: any | null = null;

  constructor(private service: RoadmapService) {}

  save() {
    if (!this.title) return;
    this.saving = true;
    const r: Roadmap = { title: this.title, description: this.description };
    this.service.create(r).subscribe({
      next: () => {
        this.title = '';
        this.description = '';
        this.saving = false;
        this.service.notifyRoadmapsChanged();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) return;

    this.uploadError = '';
    this.uploaded = 0;

    const reader = new FileReader();
    reader.onload = () => {
      try {
        const raw = JSON.parse(String(reader.result || ''));
        const items = this.extractItems(raw);
        if (!Array.isArray(items)) {
          this.uploadError = 'Formato JSON no valido. Se esperaba un array o { roadmaps: [...] }.';
          this.preview = null;
          return;
        }
        this.preview = this.pickPreview(items);

        const unique = new Set<string>();
        const roadmaps: Roadmap[] = items
          .map((r: any) => ({
            title: String(r?.title || '').trim(),
            description: String(r?.description || '').trim()
          }))
          .filter((r: Roadmap) => {
            if (r.title.length === 0) return false;
            const key = `${r.title}::${r.description}`;
            if (unique.has(key)) return false;
            unique.add(key);
            return true;
          });

        if (roadmaps.length === 0) {
          this.uploadError = 'No hay roadmaps validos para cargar.';
          return;
        }

        this.uploading = true;
        from(roadmaps)
          .pipe(
            concatMap(r => this.service.create(r)),
            finalize(() => {
              this.uploading = false;
              input.value = '';
            })
          )
          .subscribe({
            next: () => { this.uploaded += 1; },
            error: () => { this.uploadError = 'Fallo la carga de roadmaps.'; },
            complete: () => { this.service.notifyRoadmapsChanged(); }
          });
      } catch {
        this.uploadError = 'No se pudo leer el JSON.';
        this.preview = null;
      }
    };
    reader.onerror = () => { this.uploadError = 'No se pudo leer el archivo.'; };
    reader.readAsText(file);
  }

  private extractItems(raw: any): any[] | null {
    if (!Array.isArray(raw) && raw && typeof raw === 'object' && typeof raw.title === 'string') {
      return [raw];
    }
    if (Array.isArray(raw)) {
      return raw;
    }
    if (Array.isArray(raw?.roadmaps)) {
      return raw.roadmaps;
    }
    return null;
  }

  private pickPreview(items: any[]): any | null {
    if (items.length === 0) return null;
    return items
      .filter((r: any) => r && typeof r === 'object')
      .sort((a: any, b: any) => this.scoreRoadmap(b) - this.scoreRoadmap(a))[0] || null;
  }

  private scoreRoadmap(r: any): number {
    const ejes = Array.isArray(r?.ejes_estrategicos) ? r.ejes_estrategicos.length : 0;
    const iniciativas = Array.isArray(r?.iniciativas) ? r.iniciativas.length : 0;
    const hasDescription = String(r?.description || '').trim().length > 0 ? 1 : 0;
    return (ejes * 10) + (iniciativas * 10) + hasDescription;
  }
}
