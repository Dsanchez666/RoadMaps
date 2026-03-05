import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: false,
  template: `
    <div class="container">
      <h1>Roadmaps</h1>
      <app-database-config></app-database-config>
      <app-roadmap-create></app-roadmap-create>
      <app-roadmap-list></app-roadmap-list>
    </div>
  `
})
export class AppComponent {}
