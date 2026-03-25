import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DatabaseService } from '../../../database/services/database.service';

@Component({
  selector: 'app-roadmap-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  connected = false;
  private readonly destroy$ = new Subject<void>();

  constructor(private databaseService: DatabaseService) {}

  ngOnInit(): void {
    this.databaseService.status$
      .pipe(takeUntil(this.destroy$))
      .subscribe((status) => {
        this.connected = Boolean(status?.connected);
      });

    this.databaseService.getStatus().subscribe({
      next: (status) => { this.connected = Boolean(status?.connected); },
      error: () => { this.connected = false; }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onConnectedChange(connected: boolean) {
    this.connected = connected;
  }
}