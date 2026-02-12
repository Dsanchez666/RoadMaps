import { Component } from '@angular/core';
import { RoadmapService, Roadmap } from '../../services/roadmap.service';

@Component({
  selector: 'app-roadmap-create',
  templateUrl: './roadmap-create.component.html',
  styleUrls: ['./roadmap-create.component.scss']
})
export class RoadmapCreateComponent {
  title = '';
  description = '';
  saving = false;

  constructor(private service: RoadmapService) {}

  save() {
    if (!this.title) return;
    this.saving = true;
    const r: Roadmap = { title: this.title, description: this.description };
    this.service.create(r).subscribe(() => {
      this.title = '';
      this.description = '';
      this.saving = false;
    });
  }
}
