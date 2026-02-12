import { Component, OnInit } from '@angular/core';
import { RoadmapService, Roadmap } from '../../services/roadmap.service';

@Component({
  selector: 'app-roadmap-list',
  templateUrl: './roadmap-list.component.html',
  styleUrls: ['./roadmap-list.component.scss']
})
export class RoadmapListComponent implements OnInit {
  roadmaps: Roadmap[] = [];
  loading = false;

  constructor(private service: RoadmapService) {}

  ngOnInit(): void { this.load(); }

  load() {
    this.loading = true;
    this.service.list().subscribe(r => { this.roadmaps = r; this.loading = false; });
  }
}
