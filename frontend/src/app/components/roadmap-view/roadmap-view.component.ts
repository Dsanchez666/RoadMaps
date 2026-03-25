import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ConnectionStateService } from '../../services/connection-state.service';
import { InitiativeConfig, Roadmap, RoadmapConfig, RoadmapService } from '../../services/roadmap.service';

interface TimelineSlot {
  key: string;
  year: number;
  quarter: number;
}

interface TimelineYearGroup {
  year: number;
  span: number;
}

/**
 * RoadmapViewComponent
 *
 * Read-only view for one roadmap and its editable configuration.
 */
@Component({
  selector: 'app-roadmap-view',
  standalone: false,
  templateUrl: './roadmap-view.component.html',
  styleUrls: ['./roadmap-view.component.scss']
})
export class RoadmapViewComponent implements OnInit {
  roadmap: Roadmap | null = null;
  config: RoadmapConfig | null = null;
  readonly suggestedAdditionalKeys = ['tipo', 'expediente', 'objetivo', 'impacto_principal', 'usuarios_afectados'];
  timeline: TimelineSlot[] = [];
  timelineYearGroups: TimelineYearGroup[] = [];
  initiativesByAxis: Record<string, InitiativeConfig[]> = {};
  loading = false;
  savingInitiative = false;
  error = '';
  saveMessage = '';
  reconnecting = false;
  hoverFocusIds: Set<string> | null = null;
  initiativeModalOpen = false;
  editingInitiativeId = '';
  initiativeDraft: InitiativeConfig | null = null;
  dependenciesInput = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly roadmapService: RoadmapService,
    private readonly connectionState: ConnectionStateService
  ) {}

  /**
   * Loads roadmap and associated edit configuration.
   */
  ngOnInit(): void {
    this.load();
  }

  /**
   * Navigates back to roadmap list.
   */
  volver(): void {
    this.router.navigate(['/roadmaps']);
  }

  /**
   * Navigates to edit screen for current roadmap.
   */
  editar(): void {
    if (!this.roadmap?.id) {
      return;
    }
    this.router.navigate(['/roadmaps', this.roadmap.id, 'edit']);
  }

  /**
   * Attempts reconnect then reloads current screen.
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
   * Main load workflow: validates DB status and fetches roadmap data.
   */
  private load(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Id de roadmap no informado en la ruta.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.connectionState.refreshStatus().subscribe({ error: () => void 0 });
    this.roadmapService.get(id).subscribe({
      next: (roadmap) => {
        this.roadmap = roadmap;
        this.roadmapService.getConfig(id).subscribe({
          next: (config) => {
            const normalized = this.ensureConfigDefaults(config);
            this.config = normalized;
            this.timeline = this.buildTimeline(normalized);
            this.timelineYearGroups = this.buildYearGroups(this.timeline);
            this.initiativesByAxis = this.groupInitiativesByAxis(normalized);
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

  axisInitiatives(axisId: string): InitiativeConfig[] {
    return this.initiativesByAxis[axisId] || [];
  }

  onInitiativeHover(initiative: InitiativeConfig): void {
    if (!this.config) {
      return;
    }
    const focus = new Set<string>();
    focus.add(initiative.id);

    for (const dep of initiative.dependencias || []) {
      if (dep.iniciativa) {
        focus.add(dep.iniciativa);
      }
    }

    for (const ini of this.config.iniciativas || []) {
      if ((ini.dependencias || []).some((d) => d.iniciativa === initiative.id)) {
        focus.add(ini.id);
      }
    }
    this.hoverFocusIds = focus;
  }

  clearInitiativeHover(): void {
    this.hoverFocusIds = null;
  }

  isDimmed(initiative: InitiativeConfig): boolean {
    if (!this.hoverFocusIds || this.hoverFocusIds.size === 0) {
      return false;
    }
    return !this.hoverFocusIds.has(initiative.id);
  }

  openInitiativeModal(initiative: InitiativeConfig): void {
    this.saveMessage = '';
    this.error = '';
    this.editingInitiativeId = initiative.id;
    this.initiativeDraft = this.normalizeInitiative(JSON.parse(JSON.stringify(initiative)));
    this.dependenciesInput = (initiative.dependencias || [])
      .map((d) => d.iniciativa)
      .filter((v) => !!v)
      .join(', ');
    this.initiativeModalOpen = true;
  }

  closeInitiativeModal(): void {
    this.initiativeModalOpen = false;
    this.initiativeDraft = null;
    this.editingInitiativeId = '';
    this.dependenciesInput = '';
  }

  saveInitiativeChanges(): void {
    if (!this.config || !this.roadmap?.id || !this.initiativeDraft) {
      return;
    }

    const index = this.config.iniciativas.findIndex((i) => i.id === this.editingInitiativeId);
    if (index < 0) {
      this.error = 'No se encontró la iniciativa a actualizar.';
      return;
    }

    const currentDeps = new Map<string, string>();
    for (const dep of this.config.iniciativas[index].dependencias || []) {
      currentDeps.set(dep.iniciativa, dep.tipo || 'funcional');
    }
    const depIds = this.dependenciesInput
      .split(',')
      .map((v) => v.trim())
      .filter((v) => v.length > 0);
    this.initiativeDraft.dependencias = depIds.map((id) => ({
      iniciativa: id,
      tipo: currentDeps.get(id) || 'funcional'
    }));

    this.config.iniciativas[index] = this.normalizeInitiative(JSON.parse(JSON.stringify(this.initiativeDraft)));
    this.savingInitiative = true;
    this.roadmapService.saveConfig(this.roadmap.id, this.config).subscribe({
      next: () => {
        this.savingInitiative = false;
        this.saveMessage = 'Iniciativa actualizada correctamente.';
        this.initiativesByAxis = this.groupInitiativesByAxis(this.config!);
        this.closeInitiativeModal();
      },
      error: (err) => {
        this.savingInitiative = false;
        this.error = err?.error?.message || 'No se pudo guardar cambios de la iniciativa.';
      }
    });
  }

  isFilled(initiative: InitiativeConfig, slot: TimelineSlot): boolean {
    const start = this.parseQuarter(initiative.inicio);
    const end = this.parseQuarter(initiative.fin);
    if (!start || !end) {
      return false;
    }
    const current = slot.year * 10 + slot.quarter;
    const startValue = start.year * 10 + start.quarter;
    const endValue = end.year * 10 + end.quarter;
    return current >= startValue && current <= endValue;
  }

  certaintyClass(value: string): string {
    const v = (value || '').toLowerCase();
    if (v === 'comprometido') {
      return 'certeza-comprometido';
    }
    if (v === 'exploratorio') {
      return 'certeza-exploratorio';
    }
    return 'certeza-planificado';
  }

  trackByQuarter(_: number, slot: TimelineSlot): string {
    return slot.key;
  }

  /**
   * Returns one additional-data value from an initiative.
   */
  getAdditionalValue(initiative: InitiativeConfig, key: string): string {
    return initiative?.informacion_adicional?.[key] || '';
  }

  /**
   * Returns dynamic additional-data keys currently set in modal draft.
   */
  draftAdditionalKeys(): string[] {
    return Object.keys(this.initiativeDraft?.informacion_adicional || {});
  }

  /**
   * Returns suggested dynamic keys still absent in modal draft.
   */
  availableDraftSuggestedKeys(): string[] {
    const existing = new Set(this.draftAdditionalKeys());
    return this.suggestedAdditionalKeys.filter((key) => !existing.has(key));
  }

  /**
   * Adds one empty dynamic field to modal draft.
   */
  addDraftAdditionalField(): void {
    if (!this.initiativeDraft) {
      return;
    }
    if (!this.initiativeDraft.informacion_adicional) {
      this.initiativeDraft.informacion_adicional = {};
    }
    const existing = new Set(Object.keys(this.initiativeDraft.informacion_adicional));
    let candidate = 'nuevo_campo';
    let suffix = 1;
    while (existing.has(candidate)) {
      suffix += 1;
      candidate = `nuevo_campo_${suffix}`;
    }
    this.initiativeDraft.informacion_adicional[candidate] = '';
  }

  /**
   * Adds one suggested dynamic key to modal draft.
   */
  addDraftSuggestedField(key: string): void {
    if (!this.initiativeDraft) {
      return;
    }
    const normalized = (key || '').trim();
    if (!normalized) {
      return;
    }
    if (!this.initiativeDraft.informacion_adicional) {
      this.initiativeDraft.informacion_adicional = {};
    }
    if (!Object.prototype.hasOwnProperty.call(this.initiativeDraft.informacion_adicional, normalized)) {
      this.initiativeDraft.informacion_adicional[normalized] = '';
    }
  }

  /**
   * Renames one dynamic field key in modal draft.
   */
  renameDraftAdditionalKey(oldKey: string, newKey: string): void {
    if (!this.initiativeDraft?.informacion_adicional) {
      return;
    }
    const nextKey = (newKey || '').trim();
    if (!nextKey || nextKey === oldKey) {
      return;
    }
    if (Object.prototype.hasOwnProperty.call(this.initiativeDraft.informacion_adicional, nextKey)) {
      return;
    }
    const currentValue = this.initiativeDraft.informacion_adicional[oldKey];
    delete this.initiativeDraft.informacion_adicional[oldKey];
    this.initiativeDraft.informacion_adicional[nextKey] = currentValue;
  }

  /**
   * Updates one dynamic field value in modal draft.
   */
  updateDraftAdditionalValue(key: string, value: string): void {
    if (!this.initiativeDraft) {
      return;
    }
    if (!this.initiativeDraft.informacion_adicional) {
      this.initiativeDraft.informacion_adicional = {};
    }
    this.initiativeDraft.informacion_adicional[key] = value;
  }

  /**
   * Removes one dynamic field from modal draft.
   */
  removeDraftAdditionalField(key: string): void {
    if (!this.initiativeDraft?.informacion_adicional) {
      return;
    }
    delete this.initiativeDraft.informacion_adicional[key];
  }

  private buildTimeline(config: RoadmapConfig): TimelineSlot[] {
    const start = this.parseQuarter(config.horizonte_base?.inicio);
    const end = this.parseQuarter(config.horizonte_base?.fin);
    if (!start || !end) {
      return [];
    }

    const slots: TimelineSlot[] = [];
    for (let year = start.year; year <= end.year; year++) {
      const qStart = year === start.year ? start.quarter : 1;
      const qEnd = year === end.year ? end.quarter : 4;
      for (let quarter = qStart; quarter <= qEnd; quarter++) {
        slots.push({ key: `${year}-T${quarter}`, year, quarter });
      }
    }
    return slots;
  }

  private buildYearGroups(slots: TimelineSlot[]): TimelineYearGroup[] {
    const groups: TimelineYearGroup[] = [];
    for (const slot of slots) {
      const last = groups[groups.length - 1];
      if (!last || last.year !== slot.year) {
        groups.push({ year: slot.year, span: 1 });
      } else {
        last.span += 1;
      }
    }
    return groups;
  }

  private groupInitiativesByAxis(config: RoadmapConfig): Record<string, InitiativeConfig[]> {
    const result: Record<string, InitiativeConfig[]> = {};
    for (const initiative of config.iniciativas || []) {
      const axis = initiative.eje || 'SIN_EJE';
      if (!result[axis]) {
        result[axis] = [];
      }
      result[axis].push(initiative);
    }
    return result;
  }

  private parseQuarter(value?: string): { year: number; quarter: number } | null {
    if (!value) {
      return null;
    }
    const match = /^(\d{4})-T([1-4])$/.exec(value.trim());
    if (!match) {
      return null;
    }
    return { year: Number(match[1]), quarter: Number(match[2]) };
  }

  /**
   * Normalizes API payload so view and modal logic always receive complete structures.
   */
  private ensureConfigDefaults(config: Partial<RoadmapConfig>): RoadmapConfig {
    return {
      producto: String(config?.producto || ''),
      organizacion: String(config?.organizacion || ''),
      horizonte_base: {
        inicio: String(config?.horizonte_base?.inicio || ''),
        fin: String(config?.horizonte_base?.fin || '')
      },
      ejes_estrategicos: config?.ejes_estrategicos || [],
      iniciativas: (config?.iniciativas || []).map((initiative: any) => this.normalizeInitiative(initiative))
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

    this.copyLegacyField(raw, 'tipo', output);
    this.copyLegacyField(raw, 'expediente', output);
    this.copyLegacyField(raw, 'objetivo', output);
    this.copyLegacyField(raw, 'impacto_principal', output);
    this.copyLegacyField(raw, 'usuarios_afectados', output);
    return output;
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
