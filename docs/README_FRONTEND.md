Frontend Angular - guía rápida

Se recomienda generar la app Angular usando Angular CLI:

```bash
npm install -g @angular/cli
ng new frontend --routing=false --style=scss
cd frontend
ng generate service services/roadmap
ng generate component components/roadmap-list
ng generate component components/roadmap-create
```

Ejemplo de `roadmap.service.ts` (cliente HTTP):

```ts
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
```

Recuerda configurar proxy para desarrollo (`proxy.conf.json`) para redirigir `/api` al backend (por ejemplo `http://localhost:8080`).
