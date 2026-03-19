import { Component } from '@angular/core';
import { from } from 'rxjs';
import { concatMap, finalize } from 'rxjs/operators';
import { RoadmapService } from '../../services/roadmap.service';
import { Roadmap } from '../../../../shared/models/roadmap.model';
import { buildImportResult } from '../../utils/roadmap-import';

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
        const result = buildImportResult(raw);

        this.preview = result.preview;
        if (result.error) {
          this.uploadError = result.error;
          return;
        }

        this.uploading = true;
        from(result.roadmaps)
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
}
