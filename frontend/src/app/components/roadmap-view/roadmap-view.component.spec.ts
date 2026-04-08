import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { ConnectionStateService } from '../../services/connection-state.service';
import { RoadmapService } from '../../services/roadmap.service';
import { RoadmapViewComponent } from './roadmap-view.component';

describe('RoadmapViewComponent', () => {
  let component: RoadmapViewComponent;
  let fixture: ComponentFixture<RoadmapViewComponent>;
  let roadmapServiceSpy: jasmine.SpyObj<RoadmapService>;
  let connectionStateSpy: jasmine.SpyObj<ConnectionStateService>;

  beforeEach(async () => {
    roadmapServiceSpy = jasmine.createSpyObj<RoadmapService>('RoadmapService', [
      'get',
      'getConfig',
      'saveConfig'
    ]);
    connectionStateSpy = jasmine.createSpyObj<ConnectionStateService>('ConnectionStateService', [
      'refreshStatus',
      'reconnect'
    ]);

    roadmapServiceSpy.get.and.returnValue(of({ id: 'rm-1', title: 'ETNA', description: '' }));
    roadmapServiceSpy.getConfig.and.returnValue(of({
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [],
      expedientes_catalogo: [],
      compromisos: []
    }));
    roadmapServiceSpy.saveConfig.and.returnValue(of(void 0));
    connectionStateSpy.refreshStatus.and.returnValue(of(null));
    connectionStateSpy.reconnect.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      declarations: [RoadmapViewComponent],
      imports: [FormsModule, RouterTestingModule],
      providers: [
        { provide: RoadmapService, useValue: roadmapServiceSpy },
        { provide: ConnectionStateService, useValue: connectionStateSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => 'rm-1'
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RoadmapViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should build expedition rows from structured expedientes and legacy additional info', () => {
    component.config = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [
        {
          id: 'I1',
          nombre: 'Iniciativa 1',
          eje: 'E1',
          inicio: '2026-T1',
          fin: '2026-T2',
          certeza: 'planificado',
          dependencias: [],
          informacion_adicional: {},
          expedientes: [
            {
              id: 'EXP-1',
              tipo: 'licitacion',
              empresa: 'Empresa A',
              expediente: 'DNA-100',
              impacto: 'alto',
              precio_licitacion: '1000',
              precio_adjudicacion: '900',
              fecha_fin_expediente: '2026-10-10',
              informacion_adicional: { lote: '1' }
            }
          ]
        },
        {
          id: 'I2',
          nombre: 'Iniciativa 2',
          eje: 'E1',
          inicio: '2026-T2',
          fin: '2026-T3',
          certeza: 'comprometido',
          dependencias: [],
          informacion_adicional: {
            expediente: 'DNA-200',
            empresa: 'Empresa B',
            precio_licitacion: '2000'
          },
          expedientes: []
        }
      ],
      expedientes_catalogo: [],
      compromisos: []
    };

    const rows = component.expeditionRows();

    expect(rows.length).toBe(2);
    expect(rows[0].expediente).toBe('DNA-100');
    expect(rows[0].initiativeName).toBe('Iniciativa 1');
    expect(rows[1].expediente).toBe('DNA-200');
    expect(rows[1].empresa).toBe('Empresa B');
  });

  it('should save a new commitment and expose it in the list', () => {
    component.roadmap = { id: 'rm-1', title: 'ETNA', description: '' };
    component.config = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [],
      expedientes_catalogo: [],
      compromisos: []
    };
    component.commitmentDraft = {
      id: '',
      descripcion: 'Compromiso de ejemplo',
      fecha_comprometido: '2026-05-01',
      actor: 'Producto',
      quien_compromete: 'Direccion',
      informacion_adicional: { estado: 'pendiente' }
    };

    component.addCommitment();

    expect(roadmapServiceSpy.saveConfig).toHaveBeenCalled();
    expect(component.commitments().length).toBe(1);
    expect(component.commitments()[0].descripcion).toBe('Compromiso de ejemplo');
    expect(component.showCommitmentForm).toBeFalse();
  });

  it('should calculate expedition totals from filtered rows', () => {
    component.config = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [
        {
          id: 'I1',
          nombre: 'Iniciativa 1',
          eje: 'E1',
          inicio: '2026-T1',
          fin: '2026-T2',
          certeza: 'planificado',
          dependencias: [],
          informacion_adicional: {},
          expedientes: [
            {
              id: 'EXP-1',
              tipo: '',
              empresa: '',
              expediente: 'DNA-100',
              impacto: '',
              precio_licitacion: '1000',
              precio_adjudicacion: '800',
              fecha_fin_expediente: '',
              informacion_adicional: {}
            },
            {
              id: 'EXP-2',
              tipo: '',
              empresa: '',
              expediente: 'DNA-200',
              impacto: '',
              precio_licitacion: '2000',
              precio_adjudicacion: '1500',
              fecha_fin_expediente: '',
              informacion_adicional: {}
            }
          ]
        }
      ],
      expedientes_catalogo: [],
      compromisos: []
    };

    component.expeditionSearch = 'DNA-100';
    const summary = component.expeditionSummary();

    expect(summary.totalLicitacion).toBe(1000);
    expect(summary.totalAdjudicacion).toBe(800);
    expect(summary.averageDiscount).toBeCloseTo(20, 3);
  });

  it('should link one existing expediente only once', () => {
    component.config = {
      producto: 'ETNA',
      organizacion: 'ENAIRE',
      horizonte_base: { inicio: '2026-T1', fin: '2030-T4' },
      ejes_estrategicos: [],
      iniciativas: [],
      expedientes_catalogo: [
        {
          id: 'EXP-1',
          tipo: 'licitacion',
          empresa: 'Empresa A',
          expediente: 'DNA-100',
          impacto: '',
          precio_licitacion: '1000',
          precio_adjudicacion: '900',
          fecha_fin_expediente: '2026-10-10',
          informacion_adicional: {}
        }
      ],
      compromisos: []
    };
    component.initiativeDraft = {
      id: 'I1',
      nombre: 'Iniciativa 1',
      eje: 'E1',
      inicio: '2026-T1',
      fin: '2026-T2',
      certeza: 'planificado',
      informacion_adicional: {},
      expedientes: [],
      dependencias: []
    };

    component.linkExistingExpediente(component.config.expedientes_catalogo[0]);
    component.linkExistingExpediente(component.config.expedientes_catalogo[0]);

    expect(component.draftExpedientes().length).toBe(1);
    expect(component.draftExpedientes()[0].id).toBe('EXP-1');
  });
});
