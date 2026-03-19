import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DatabaseConfigComponent } from './features/database/components/database-config/database-config.component';
import { RoadmapCreateComponent } from './features/roadmaps/components/roadmap-create/roadmap-create.component';
import { RoadmapListComponent } from './features/roadmaps/components/roadmap-list/roadmap-list.component';
import { HomeComponent } from './features/roadmaps/components/roadmap-home/home.component';
import { RoadmapViewComponent } from './features/roadmaps/components/roadmap-view/roadmap-view.component';
import { RoadmapEditComponent } from './features/roadmaps/components/roadmap-edit/roadmap-edit.component';
import { RoadmapTimelineComponent } from './features/roadmaps/components/roadmap-timeline/roadmap-timeline.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    RoadmapListComponent,
    RoadmapCreateComponent,
    DatabaseConfigComponent,
    RoadmapViewComponent,
    RoadmapEditComponent,
    RoadmapTimelineComponent
  ],
  imports: [BrowserModule, HttpClientModule, FormsModule, AppRoutingModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
