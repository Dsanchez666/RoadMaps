import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Roadmap } from '../../../shared/models/roadmap.model';

@Injectable({ providedIn: 'root' })
export class RoadmapService {
  base = '/api/roadmaps';
  private readonly roadmapsChangedSubject = new Subject<void>();
  readonly roadmapsChanged$ = this.roadmapsChangedSubject.asObservable();

  constructor(private http: HttpClient) {}
  create(r: Roadmap): Observable<Roadmap> { return this.http.post<Roadmap>(this.base, r); }
  list(): Observable<Roadmap[]> { return this.http.get<Roadmap[]>(this.base); }
  get(id: string) { return this.http.get<Roadmap>(`${this.base}/${id}`); }
  notifyRoadmapsChanged() { this.roadmapsChangedSubject.next(); }
}
