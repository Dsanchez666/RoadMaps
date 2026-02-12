import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { RoadmapListComponent } from './components/roadmap-list/roadmap-list.component';
import { RoadmapCreateComponent } from './components/roadmap-create/roadmap-create.component';

@NgModule({
  declarations: [AppComponent, RoadmapListComponent, RoadmapCreateComponent],
  imports: [BrowserModule, HttpClientModule, FormsModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
