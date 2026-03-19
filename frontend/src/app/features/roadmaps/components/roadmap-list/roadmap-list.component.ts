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
  private readonly destroy$ = new Subject<void>();

  constructor(private service: RoadmapService) {}

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
