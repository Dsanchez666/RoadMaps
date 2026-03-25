import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';

/** Core roadmap data structure used by UI and API calls. */
export interface Roadmap {
  id?: string;
  title: string;
  description?: string;
  createdAt?: string;
}

/** Strategic axis used in roadmap editing UI. */
export interface AxisConfig {
  id: string;
  nombre: string;
  descripcion: string;
  color: string;
}

/** Dependency item for one initiative. */
export interface InitiativeDependency {
  iniciativa: string;
  tipo: string;
}

/** Initiative definition used in roadmap editing UI. */
export interface InitiativeConfig {
  id: string;
  nombre: string;
  eje: string;
  inicio: string;
  fin: string;
  certeza: string;
  informacion_adicional: Record<string, string>;
  dependencias: InitiativeDependency[];
}

/** Full editable roadmap configuration persisted in local storage. */
export interface RoadmapConfig {
  producto: string;
  organizacion: string;
  horizonte_base: {
    inicio: string;
    fin: string;
  };
  ejes_estrategicos: AxisConfig[];
  iniciativas: InitiativeConfig[];
}

/** Full payload accepted by roadmap JSON import endpoint. */
export interface RoadmapImportPayload {
  title?: string;
  description?: string;
  producto?: string;
  organizacion?: string;
  horizonte_base?: {
    inicio?: string;
    fin?: string;
  };
  ejes_estrategicos?: AxisConfig[];
  iniciativas?: InitiativeConfig[];
}

/**
 * Service responsible for roadmap CRUD operations.
 *
 * This service provides methods for creating, listing and reading roadmaps,
 * and emits a synchronization event when list data should be refreshed.
 *
 * @providedIn 'root'
 */
@Injectable({ providedIn: 'root' })
export class RoadmapService {
  base = '/api/roadmaps';
  private readonly roadmapsChangedSubject = new Subject<void>();
  readonly roadmapsChanged$ = this.roadmapsChangedSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Creates a new roadmap.
   *
   * @param r Roadmap payload.
   * @returns Observable<Roadmap> Created roadmap.
   */
  create(r: Roadmap): Observable<Roadmap> { return this.http.post<Roadmap>(this.base, r); }

  /**
   * Lists all available roadmaps.
   *
   * @returns Observable<Roadmap[]> Roadmap collection.
   */
  list(): Observable<Roadmap[]> { return this.http.get<Roadmap[]>(this.base); }

  /**
   * Retrieves one roadmap by id.
   *
   * @param id Roadmap identifier.
   * @returns Observable<Roadmap> Requested roadmap.
   */
  get(id: string) { return this.http.get<Roadmap>(`${this.base}/${id}`); }

  /**
   * Reads editable roadmap configuration from backend persistence.
   *
   * @param id Roadmap identifier.
   * @returns Observable<RoadmapConfig> saved configuration.
   */
  getConfig(id: string): Observable<RoadmapConfig> {
    return this.http.get<RoadmapConfig>(`${this.base}/${id}/config`);
  }

  /**
   * Persists editable roadmap configuration in backend persistence.
   *
   * @param id Roadmap identifier.
   * @param config Configuration payload.
   * @returns Observable<void> completion signal.
   */
  saveConfig(id: string, config: RoadmapConfig): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}/config`, config);
  }

  /**
   * Imports one full roadmap JSON payload and persists it in backend.
   *
   * @param payload JSON roadmap payload.
   * @returns Observable<Roadmap> created roadmap header data.
   */
  importRoadmap(payload: RoadmapImportPayload): Observable<Roadmap> {
    return this.http.post<Roadmap>(`${this.base}/import`, payload);
  }

  /**
   * Emits a signal so subscribers can refresh roadmap list data.
   */
  notifyRoadmapsChanged() { this.roadmapsChangedSubject.next(); }
}
