import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { RoadmapConfig, RoadmapService } from './roadmap.service';

describe('RoadmapService', () => {
  let service: RoadmapService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(RoadmapService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request roadmap config with GET /api/roadmaps/:id/config', () => {
    const config: RoadmapConfig = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [],
      compromisos: []
    };

    service.getConfig('rm-1').subscribe((result) => {
      expect(result.producto).toBe('ETNA');
    });

    const req = httpMock.expectOne('/api/roadmaps/rm-1/config');
    expect(req.request.method).toBe('GET');
    req.flush(config);
  });

  it('should persist roadmap config with PUT /api/roadmaps/:id/config', () => {
    const config: RoadmapConfig = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [
        {
          id: 'I1',
          nombre: 'Init',
          eje: 'E1',
          inicio: '2026-T1',
          fin: '2026-T2',
          certeza: 'planificado',
          informacion_adicional: { tipo: 'mantenimiento', expediente: 'DNA-1' },
          expedientes: [],
          dependencias: []
        }
      ],
      compromisos: []
    };

    service.saveConfig('rm-2', config).subscribe();

    const req = httpMock.expectOne('/api/roadmaps/rm-2/config');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.producto).toBe('ETNA');
    expect(req.request.body.iniciativas[0].informacion_adicional.tipo).toBe('mantenimiento');
    req.flush({});
  });

  it('should import roadmap with POST /api/roadmaps/import', () => {
    const payload = {
      title: 'ETNA',
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [],
      compromisos: []
    };

    service.importRoadmap(payload).subscribe((roadmap) => {
      expect(roadmap.title).toBe('ETNA');
    });

    const req = httpMock.expectOne('/api/roadmaps/import');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'rm-new', title: 'ETNA', description: '' });
  });
});
