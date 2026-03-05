import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { DatabaseConfigComponent } from './components/database-config/database-config.component';
import { RoadmapCreateComponent } from './components/roadmap-create/roadmap-create.component';
import { RoadmapListComponent } from './components/roadmap-list/roadmap-list.component';

@NgModule({
  declarations: [AppComponent, RoadmapListComponent, RoadmapCreateComponent, DatabaseConfigComponent],
  imports: [BrowserModule, HttpClientModule, FormsModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
