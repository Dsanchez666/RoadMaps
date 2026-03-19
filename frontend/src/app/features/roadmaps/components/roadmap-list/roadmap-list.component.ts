import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoadmapService } from '../../services/roadmap.service';
import { Roadmap } from '../../../../shared/models/roadmap.model';

@Component({
  selector: 'app-roadmap-list',
  standalone: false,
  templateUrl: './roadmap-list.component.html',
  styleUrls: ['./roadmap-list.component.scss']
})
export class RoadmapListComponent implements OnInit, OnDestroy {
  roadmaps: Roadmap[] = [];
  loading = false;
  searchTerm = '';
  private readonly destroy$ = new Subject<void>();

  constructor(private service: RoadmapService) {}

  get filteredRoadmaps(): Roadmap[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.roadmaps;
    }
    return this.roadmaps.filter(r =>
      (r.title || '').toLowerCase().includes(term) ||
      (r.description || '').toLowerCase().includes(term) ||
      (r.id || '').toLowerCase().includes(term)
    );
  }

  ngOnInit(): void {
    this.load();
    this.service.roadmapsChanged$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.load());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load() {
    this.loading = true;
    this.service.list().subscribe(r => { this.roadmaps = r; this.loading = false; });
  }
}
