import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DatabaseConfigComponent } from './components/database-config/database-config.component';
import { RoadmapEditComponent } from './components/roadmap-edit/roadmap-edit.component';
import { RoadmapListComponent } from './components/roadmap-list/roadmap-list.component';
import { RoadmapViewComponent } from './components/roadmap-view/roadmap-view.component';

/**
 * Router configuration for the Angular frontend.
 *
 * Centralizes screen navigation so the UI flow remains explicit and testable.
 */
const routes: Routes = [
  { path: 'database', component: DatabaseConfigComponent },
  { path: 'roadmaps', component: RoadmapListComponent },
  { path: 'roadmaps/:id/view', component: RoadmapViewComponent },
  { path: 'roadmaps/:id/edit', component: RoadmapEditComponent },
  { path: '', pathMatch: 'full', redirectTo: 'database' },
  { path: '**', redirectTo: 'database' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
