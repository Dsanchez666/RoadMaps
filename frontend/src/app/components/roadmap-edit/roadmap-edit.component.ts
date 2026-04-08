import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  AxisConfig,
  CommitmentConfig,
  InitiativeExpediente,
  InitiativeConfig,
  Roadmap,
  RoadmapConfig,
  RoadmapService
} from '../../services/roadmap.service';
import { ConnectionStateService } from '../../services/connection-state.service';

/**
 * RoadmapEditComponent
 *
 * Editor for roadmap product data, strategic axes and initiatives.
 */
@Component({
  selector: 'app-roadmap-edit',
  standalone: false,
  templateUrl: './roadmap-edit.component.html',
  styleUrls: ['./roadmap-edit.component.scss']
})
export class RoadmapEditComponent implements OnInit {
  roadmap: Roadmap | null = null;
  config: RoadmapConfig | null = null;
  readonly suggestedAdditionalKeys = [
    'tipo',
    'expediente',
    'precio_licitacion',
    'precio_adjudicacion',
    'empresa',
    'fecha_fin_expediente',
    'objetivo',
    'impacto_principal',
    'usuarios_afectados'
  ];
  loading = false;
  saving = false;
  error = '';
  success = '';
  reconnecting = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly roadmapService: RoadmapService,
    private readonly connectionState: ConnectionStateService
  ) {}

  /**
   * Loads roadmap and initializes editable configuration.
   */
  ngOnInit(): void {
    this.load();
  }

  /**
   * Returns back to roadmap list.
   */
  volver(): void {
    this.router.navigate(['/roadmaps']);
  }

  /**
   * Attempts reconnect and retries loading.
   */
  reconnect(): void {
    this.reconnecting = true;
    this.connectionState.reconnect().subscribe({
      next: () => {
        this.reconnecting = false;
        this.load();
      },
      error: (err) => {
        this.reconnecting = false;
        this.error = err?.message || 'No se pudo reconectar';
      }
    });
  }

  /**
   * Adds a new empty strategic axis row.
   */
  addEje(): void {
    if (!this.config) {
      return;
    }
    const nextNumber = this.config.ejes_estrategicos.length + 1;
    this.config.ejes_estrategicos.push({
      id: `E${nextNumber}`,
      nombre: '',
      descripcion: '',
      color: '#1976D2'
    });
  }

  /**
   * Removes one strategic axis by index and unassigns initiatives using it.
   */
  removeEje(index: number): void {
    if (!this.config) {
      return;
    }
    const [removed] = this.config.ejes_estrategicos.splice(index, 1);
    if (!removed) {
      return;
    }

    this.config.iniciativas = this.config.iniciativas.map((ini) => {
      if (ini.eje === removed.id) {
        return { ...ini, eje: '' };
      }
      return ini;
    });
  }

  /**
   * Adds a new empty initiative row.
   */
  addIniciativa(): void {
    if (!this.config) {
      return;
    }
    const nextNumber = this.config.iniciativas.length + 1;
    const iniciativa: InitiativeConfig = {
      id: `INIT-${nextNumber}`,
      nombre: '',
      eje: this.config.ejes_estrategicos[0]?.id || '',
      inicio: this.config.horizonte_base.inicio || '',
      fin: this.config.horizonte_base.fin || '',
      certeza: 'planificado',
      informacion_adicional: {},
      expedientes: [],
      dependencias: []
    };
    this.config.iniciativas.push(iniciativa);
  }

  /**
   * Removes initiative by index.
   */
  removeIniciativa(index: number): void {
    if (!this.config) {
      return;
    }
    this.config.iniciativas.splice(index, 1);
  }

  /**
   * Saves editable configuration in backend persistence.
   */
  guardar(): void {
    if (!this.roadmap || !this.config) {
      return;
    }
    if (!this.roadmap.id) {
      this.error = 'No se puede guardar: roadmap sin identificador.';
      return;
    }

    this.error = '';
    this.success = '';
    if (!this.config.producto.trim()) {
      this.error = 'El producto es obligatorio.';
      return;
    }

    this.saving = true;
    this.roadmapService.saveConfig(this.roadmap.id, this.config).subscribe({
      next: () => {
        this.saving = false;
        this.success = 'Configuración guardada correctamente.';
      },
      error: () => {
        this.saving = false;
        this.error = 'No se pudo guardar la configuración.';
      }
    });
  }

  /**
   * Returns available axis ids for initiative assignment dropdown.
   */
  ejesIds(): string[] {
    if (!this.config) {
      return [];
    }
    return this.config.ejes_estrategicos.map((eje) => eje.id).filter((id) => id.trim().length > 0);
  }

  /**
   * Returns dynamic additional-data keys currently set for one initiative.
   */
  additionalKeys(initiative: InitiativeConfig): string[] {
    return Object.keys(initiative.informacion_adicional || {});
  }

  /**
   * Returns suggested keys still not present in one initiative.
   */
  availableSuggestedKeys(initiative: InitiativeConfig): string[] {
    const existing = new Set(this.additionalKeys(initiative));
    return this.suggestedAdditionalKeys.filter((key) => !existing.has(key));
  }

  /**
   * Adds a new additional-data entry with generated key.
   */
  addAdditionalField(initiative: InitiativeConfig): void {
    if (!initiative.informacion_adicional) {
      initiative.informacion_adicional = {};
    }
    const existing = new Set(Object.keys(initiative.informacion_adicional));
    let candidate = 'nuevo_campo';
    let suffix = 1;
    while (existing.has(candidate)) {
      suffix += 1;
      candidate = `nuevo_campo_${suffix}`;
    }
    initiative.informacion_adicional[candidate] = '';
  }

  /**
   * Adds one suggested additional-data key when it is still missing.
   *
   * @param initiative Initiative to mutate.
   * @param key Suggested key name.
   */
  addSuggestedField(initiative: InitiativeConfig, key: string): void {
    const normalized = (key || '').trim();
    if (!normalized) {
      return;
    }
    if (!initiative.informacion_adicional) {
      initiative.informacion_adicional = {};
    }
    if (Object.prototype.hasOwnProperty.call(initiative.informacion_adicional, normalized)) {
      return;
    }
    initiative.informacion_adicional[normalized] = '';
  }

  /**
   * Renames one additional-data key while preserving its current value.
   */
  updateAdditionalKey(initiative: InitiativeConfig, oldKey: string, newKey: string): void {
    const nextKey = (newKey || '').trim();
    if (!initiative.informacion_adicional || !Object.prototype.hasOwnProperty.call(initiative.informacion_adicional, oldKey)) {
      return;
    }
    const currentValue = initiative.informacion_adicional[oldKey];
    if (!nextKey || nextKey === oldKey) {
      return;
    }
    if (Object.prototype.hasOwnProperty.call(initiative.informacion_adicional, nextKey)) {
      return;
    }
    delete initiative.informacion_adicional[oldKey];
    initiative.informacion_adicional[nextKey] = currentValue;
  }

  /**
   * Updates one additional-data value for the provided key.
   */
  updateAdditionalValue(initiative: InitiativeConfig, key: string, value: string): void {
    if (!initiative.informacion_adicional) {
      initiative.informacion_adicional = {};
    }
    initiative.informacion_adicional[key] = value;
  }

  /**
   * Removes one additional-data key/value pair.
   */
  removeAdditionalField(initiative: InitiativeConfig, key: string): void {
    if (!initiative.informacion_adicional) {
      return;
    }
    delete initiative.informacion_adicional[key];
  }

  /**
   * Main load workflow: checks backend connection then fetches roadmap.
   */
  private load(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Id de roadmap no informado en la ruta.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';
    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
    this.roadmapService.get(id).subscribe({
      next: (roadmap) => {
        this.roadmap = roadmap;
        this.roadmapService.getConfig(id).subscribe({
          next: (config) => {
            this.config = this.ensureConfigDefaults(config);
            this.loading = false;
          },
          error: (err) => {
            this.error = err?.error?.message || 'No se pudo cargar la configuración del roadmap.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message || 'No se encontró el roadmap solicitado.';
        this.loading = false;
      }
    });
  }

  /**
   * Normalizes API payload so editor always receives complete structures.
   *
   * @param config Raw config payload.
   * @returns RoadmapConfig normalized config.
   */
  private ensureConfigDefaults(config: Partial<RoadmapConfig>): RoadmapConfig {
    return {
      producto: String(config?.producto || ''),
      organizacion: String(config?.organizacion || ''),
      horizonte_base: {
        inicio: String(config?.horizonte_base?.inicio || ''),
        fin: String(config?.horizonte_base?.fin || '')
      },
      ejes_estrategicos: (config?.ejes_estrategicos || []).map((eje: AxisConfig) => ({
        id: String(eje?.id || ''),
        nombre: String(eje?.nombre || ''),
        descripcion: String(eje?.descripcion || ''),
        color: String(eje?.color || '#1976D2')
      })),
      iniciativas: (config?.iniciativas || []).map((initiative: any) => this.normalizeInitiative(initiative)),
      expedientes_catalogo: (config?.expedientes_catalogo || []).map((expediente: any) => this.normalizeSingleExpediente(expediente)),
      compromisos: (config?.compromisos || []).map((commitment: CommitmentConfig) => ({
        id: String(commitment?.id || ''),
        descripcion: String(commitment?.descripcion || ''),
        fecha_comprometido: String(commitment?.fecha_comprometido || ''),
        actor: String(commitment?.actor || ''),
        quien_compromete: String(commitment?.quien_compromete || ''),
        informacion_adicional: this.normalizeAdditionalInfo(commitment)
      }))
    };
  }

  /**
   * Produces one initiative object with guaranteed base fields and additional map.
   */
  private normalizeInitiative(raw: any): InitiativeConfig {
    return {
      id: String(raw?.id || ''),
      nombre: String(raw?.nombre || ''),
      eje: String(raw?.eje || ''),
      inicio: String(raw?.inicio || ''),
      fin: String(raw?.fin || ''),
      certeza: String(raw?.certeza || 'planificado'),
      expedientes: this.normalizeExpedientes(raw),
      dependencias: Array.isArray(raw?.dependencias) ? raw.dependencias : [],
      informacion_adicional: this.normalizeAdditionalInfo(raw)
    };
  }

  /**
   * Normalizes additional initiative data from either new dynamic format or legacy fields.
   */
  private normalizeAdditionalInfo(raw: any): Record<string, string> {
    const output: Record<string, string> = {};
    const candidate = raw?.informacion_adicional;
    if (candidate && typeof candidate === 'object' && !Array.isArray(candidate)) {
      for (const [key, value] of Object.entries(candidate)) {
        const normalizedKey = String(key || '').trim();
        if (!normalizedKey) {
          continue;
        }
        output[normalizedKey] = String(value ?? '');
      }
    }

    // Backward compatibility for payloads still exposing fixed fields.
    this.copyLegacyField(raw, 'tipo', output);
    this.copyLegacyField(raw, 'expediente', output);
    this.copyLegacyField(raw, 'precio_licitacion', output);
    this.copyLegacyField(raw, 'precio_adjudicacion', output);
    this.copyLegacyField(raw, 'empresa', output);
    this.copyLegacyField(raw, 'fecha_fin_expediente', output);
    this.copyLegacyField(raw, 'objetivo', output);
    this.copyLegacyField(raw, 'impacto_principal', output);
    this.copyLegacyField(raw, 'usuarios_afectados', output);
    return output;
  }

  /**
   * Normalizes expedientes list from payload and keeps compatibility with legacy keys.
   */
  private normalizeExpedientes(raw: any): InitiativeExpediente[] {
    const output: InitiativeExpediente[] = [];
    if (Array.isArray(raw?.expedientes)) {
      for (const item of raw.expedientes) {
        output.push(this.normalizeSingleExpediente(item));
      }
    }

    if (output.length > 0) {
      return output;
    }

    const legacyExpediente = String(raw?.expediente || raw?.informacion_adicional?.expediente || '').trim();
    const legacyEmpresa = String(raw?.empresa || raw?.informacion_adicional?.empresa || '').trim();
    const legacyLicitacion = String(raw?.precio_licitacion || raw?.informacion_adicional?.precio_licitacion || '').trim();
    const legacyAdjudicacion = String(raw?.precio_adjudicacion || raw?.informacion_adicional?.precio_adjudicacion || '').trim();
    const legacyFechaFin = String(raw?.fecha_fin_expediente || raw?.informacion_adicional?.fecha_fin_expediente || '').trim();

    if (!legacyExpediente && !legacyEmpresa && !legacyLicitacion && !legacyAdjudicacion && !legacyFechaFin) {
      return [];
    }

    return [{
      id: this.buildExpedienteId({
        tipo: String(raw?.tipo || raw?.informacion_adicional?.tipo || ''),
        empresa: legacyEmpresa,
        expediente: legacyExpediente,
        impacto: String(raw?.impacto || raw?.impacto_principal || raw?.informacion_adicional?.impacto_principal || ''),
        precio_licitacion: legacyLicitacion,
        precio_adjudicacion: legacyAdjudicacion,
        fecha_fin_expediente: legacyFechaFin,
        informacion_adicional: {}
      }),
      tipo: String(raw?.tipo || raw?.informacion_adicional?.tipo || ''),
      empresa: legacyEmpresa,
      expediente: legacyExpediente,
      impacto: String(raw?.impacto || raw?.impacto_principal || raw?.informacion_adicional?.impacto_principal || ''),
      precio_licitacion: legacyLicitacion,
      precio_adjudicacion: legacyAdjudicacion,
      fecha_fin_expediente: legacyFechaFin,
      informacion_adicional: {}
    }];
  }

  private normalizeSingleExpediente(raw: any): InitiativeExpediente {
    const normalized: InitiativeExpediente = {
      id: String(raw?.id || ''),
      tipo: String(raw?.tipo || ''),
      empresa: String(raw?.empresa || ''),
      expediente: String(raw?.expediente || ''),
      impacto: String(raw?.impacto || ''),
      precio_licitacion: String(raw?.precio_licitacion || ''),
      precio_adjudicacion: String(raw?.precio_adjudicacion || ''),
      fecha_fin_expediente: String(raw?.fecha_fin_expediente || ''),
      informacion_adicional: this.normalizeAdditionalInfo(raw)
    };
    if (!normalized.id) {
      normalized.id = this.buildExpedienteId(normalized);
    }
    return normalized;
  }

  private buildExpedienteId(expediente: Partial<InitiativeExpediente>): string {
    const raw = [
      expediente.tipo || '',
      expediente.empresa || '',
      expediente.expediente || '',
      expediente.impacto || '',
      expediente.precio_licitacion || '',
      expediente.precio_adjudicacion || '',
      expediente.fecha_fin_expediente || ''
    ]
      .join('|')
      .toLowerCase()
      .replace(/\s+/g, '-');
    return `EXP-${btoa(unescape(encodeURIComponent(raw || Date.now().toString()))).replace(/[^a-zA-Z0-9]/g, '').slice(0, 18)}`;
  }

  private copyLegacyField(raw: any, field: string, output: Record<string, string>): void {
    const value = raw?.[field];
    if (value == null || String(value).trim().length === 0) {
      return;
    }
    if (!Object.prototype.hasOwnProperty.call(output, field)) {
      output[field] = String(value);
    }
  }
}
