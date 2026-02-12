import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Roadmap { id?: string; title: string; description: string }

@Injectable({ providedIn: 'root' })
export class RoadmapService {
  base = '/api/roadmaps';
  constructor(private http: HttpClient) {}
  create(r: Roadmap): Observable<Roadmap> { return this.http.post<Roadmap>(this.base, r); }
  list(): Observable<Roadmap[]> { return this.http.get<Roadmap[]>(this.base); }
  get(id: string) { return this.http.get<Roadmap>(`${this.base}/${id}`); }
}
