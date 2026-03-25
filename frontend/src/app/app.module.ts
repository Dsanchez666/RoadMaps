import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DatabaseConfigComponent } from './components/database-config/database-config.component';
import { RoadmapEditComponent } from './components/roadmap-edit/roadmap-edit.component';
import { RoadmapCreateComponent } from './components/roadmap-create/roadmap-create.component';
import { RoadmapListComponent } from './components/roadmap-list/roadmap-list.component';
import { RoadmapViewComponent } from './components/roadmap-view/roadmap-view.component';

/**
 * Root Angular module for the Roadmaps frontend.
 *
 * Declares core components and imports the modules required for
 * browser rendering, forms and HTTP communication.
 */
@NgModule({
  declarations: [
    AppComponent,
    RoadmapListComponent,
    RoadmapCreateComponent,
    DatabaseConfigComponent,
    RoadmapViewComponent,
    RoadmapEditComponent
  ],
  imports: [BrowserModule, HttpClientModule, FormsModule, AppRoutingModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
