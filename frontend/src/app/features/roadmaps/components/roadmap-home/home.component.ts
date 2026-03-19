import { Component } from '@angular/core';
import { DatabaseService } from '../../../database/services/database.service';

@Component({
  selector: 'app-roadmap-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  connected = false;

  constructor(private databaseService: DatabaseService) {}

  onConnectedChange(connected: boolean) {
    this.connected = connected;
  }

  disconnect() {
    this.databaseService.disconnect().subscribe({
      next: () => { this.connected = false; },
      error: () => { this.connected = false; }
    });
  }
}
