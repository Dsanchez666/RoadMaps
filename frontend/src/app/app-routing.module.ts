import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './features/roadmaps/components/roadmap-home/home.component';
import { RoadmapViewComponent } from './features/roadmaps/components/roadmap-view/roadmap-view.component';
import { RoadmapEditComponent } from './features/roadmaps/components/roadmap-edit/roadmap-edit.component';

const routes: Routes = [
  { path: '', redirectTo: 'roadmaps', pathMatch: 'full' },
  { path: 'roadmaps', component: HomeComponent },
  { path: 'roadmaps/:id/view', component: RoadmapViewComponent },
  { path: 'roadmaps/:id/edit', component: RoadmapEditComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}