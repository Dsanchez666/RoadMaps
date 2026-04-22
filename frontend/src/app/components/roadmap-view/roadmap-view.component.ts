import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ConnectionStateService } from '../../services/connection-state.service';
import {
  CommitmentConfig,
  InitiativeConfig,
  InitiativeExpediente,
  Roadmap,
  RoadmapConfig,
  RoadmapService
} from '../../services/roadmap.service';

interface TimelineSlot {
  key: string;
  year: number;
  quarter: number;
}

interface TimelineYearGroup {
  year: number;
  span: number;
}

interface TimelineQuarter {
  year: number;
  quarter: number;
}

interface ExpeditionRow {
  expedienteId: string;
  initiativeId: string;
  initiativeName: string;
  initiativeIds: string[];
  initiativeNames: string[];
  initiativeExpeditionIndex: number;
  tipo: string;
  expediente: string;
  impacto: string;
  precioLicitacion: string;
  precioAdjudicacion: string;
  empresa: string;
  fechaFinExpediente: string;
  informacionAdicional: Record<string, string>;
}

interface ExpeditionSummary {
  totalLicitacion: number;
  totalAdjudicacion: number;
  averageDiscount: number;
  discountCount: number;
}

type InitiativeModalTab = 'general' | 'expedientes' | 'adicional';
type SidePanel = 'none' | 'expedientes' | 'compromisos';

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
  readonly quarterOptions = [1, 2, 3, 4];
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
  readonly commitmentAdditionalKeys = ['importe', 'estado', 'observaciones'];
  yearOptions: number[] = [2026, 2027, 2028, 2029, 2030];
  selectedStartYear = 2026;
  selectedStartQuarter = 1;
  selectedEndYear = 2030;
  selectedEndQuarter = 4;
  timeline: TimelineSlot[] = [];
  timelineYearGroups: TimelineYearGroup[] = [];
  initiativesByAxis: Record<string, InitiativeConfig[]> = {};
  showCommitmentForm = false;
  loading = false;
  savingInitiative = false;
  savingCommitment = false;
  error = '';
  saveMessage = '';
  reconnecting = false;
  hoverFocusIds: Set<string> | null = null;
  initiativeModalOpen = false;
  activeInitiativeTab: InitiativeModalTab = 'general';
  activePanel: SidePanel = 'none';
  editingInitiativeId = '';
  initiativeDraft: InitiativeConfig | null = null;
  dependenciesInput = '';
  expeditionSearch = '';
  commitmentSearch = '';
  commitmentDraft: CommitmentConfig = this.createEmptyCommitmentDraft();
  showLinkExpedienteSelector = false;
  linkExpedienteSearch = '';
  expedientesPanelWidth = 760;
  expeditionContextInitiativeIds: string[] = [];
  createInitiativeModalOpen = false;
  createInitiativeDraft: InitiativeConfig = this.createNewInitiativeDraft('');
  collapsedAxisIds: Set<string> = new Set<string>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly roadmapService: RoadmapService,
    private readonly connectionState: ConnectionStateService,
    public readonly authService: AuthService
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
            this.syncCollapsedAxes(normalized);
            this.initializeHorizonSelector(normalized);
            this.rebuildTimeline();
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
    this.activeInitiativeTab = 'general';
    this.dependenciesInput = (initiative.dependencias || [])
      .map((d) => d.iniciativa)
      .filter((v) => !!v)
      .join(', ');
    this.showLinkExpedienteSelector = false;
    this.linkExpedienteSearch = '';
    this.expeditionContextInitiativeIds = [];
    this.initiativeModalOpen = true;
  }

  closeInitiativeModal(): void {
    this.initiativeModalOpen = false;
    this.initiativeDraft = null;
    this.editingInitiativeId = '';
    this.dependenciesInput = '';
    this.activeInitiativeTab = 'general';
    this.showLinkExpedienteSelector = false;
    this.linkExpedienteSearch = '';
    this.expeditionContextInitiativeIds = [];
  }

  setInitiativeTab(tab: InitiativeModalTab): void {
    this.activeInitiativeTab = tab;
  }

  toggleAxisCollapsed(axisId: string): void {
    const normalizedId = String(axisId || '').trim();
    if (!normalizedId) {
      return;
    }
    if (this.collapsedAxisIds.has(normalizedId)) {
      this.collapsedAxisIds.delete(normalizedId);
    } else {
      this.collapsedAxisIds.add(normalizedId);
    }
  }

  isAxisCollapsed(axisId: string): boolean {
    return this.collapsedAxisIds.has(String(axisId || '').trim());
  }

  openCreateInitiativeModal(axisId: string): void {
    if (!this.canEdit()) {
      return;
    }
    this.error = '';
    this.saveMessage = '';
    this.createInitiativeDraft = this.createNewInitiativeDraft(axisId);
    this.createInitiativeModalOpen = true;
  }

  closeCreateInitiativeModal(): void {
    this.createInitiativeModalOpen = false;
  }

  saveNewInitiative(): void {
    // Bloquear si es CONSULTA
    if (this.isConsulta()) {
      console.log('⛔ CONSULTA no puede crear iniciativas');
      this.error = 'No tienes permisos para crear iniciativas';
      return;
    }

    if (!this.config || !this.roadmap?.id) {
      return;
    }

    const nombre = (this.createInitiativeDraft.nombre || '').trim();
    if (!nombre) {
      this.error = 'El nombre de la iniciativa es obligatorio.';
      return;
    }

    const axisId = (this.createInitiativeDraft.eje || '').trim();
    if (!axisId) {
      this.error = 'El eje de la iniciativa es obligatorio.';
      return;
    }

    const newId = (this.createInitiativeDraft.id || '').trim() || this.generateInitiativeIdFromName(nombre);
    if (this.config.iniciativas.some((initiative) => initiative.id === newId)) {
      this.error = `Ya existe una iniciativa con id ${newId}.`;
      return;
    }

    const newInitiative = this.normalizeInitiative({
      ...this.createInitiativeDraft,
      id: newId,
      nombre
    });
    this.config.iniciativas.push(newInitiative);
    const insertedIndex = this.config.iniciativas.length - 1;

    this.persistConfig(
      () => {
        this.saveMessage = 'Iniciativa creada correctamente.';
        this.initiativesByAxis = this.groupInitiativesByAxis(this.config!);
        this.initializeHorizonSelector(this.config!);
        this.rebuildTimeline();
        this.closeCreateInitiativeModal();
      },
      () => {
        this.config!.iniciativas.splice(insertedIndex, 1);
      }
    );
  }

  deleteInitiative(initiative: InitiativeConfig): void {
    // Bloquear si es CONSULTA
    if (this.isConsulta()) {
      console.log('⛔ CONSULTA no puede borrar iniciativas');
      return;
    }

    if (!this.config || !this.roadmap?.id) {
      return;
    }

    const index = this.config.iniciativas.findIndex((item) => item.id === initiative.id);
    if (index < 0) {
      return;
    }

    this.error = '';
    this.saveMessage = '';
    const removedInitiative = this.config.iniciativas[index];
    this.config.iniciativas.splice(index, 1);

    // Keep dependency graph consistent after deleting one initiative.
    const previousDependencias = this.config.iniciativas.map((item) => ({
      id: item.id,
      dependencias: JSON.parse(JSON.stringify(item.dependencias || []))
    }));
    for (const item of this.config.iniciativas) {
      item.dependencias = (item.dependencias || []).filter((dependency) => dependency.iniciativa !== removedInitiative.id);
    }

    this.persistConfig(
      () => {
        this.saveMessage = 'Iniciativa eliminada correctamente.';
        this.initiativesByAxis = this.groupInitiativesByAxis(this.config!);
        this.initializeHorizonSelector(this.config!);
        this.rebuildTimeline();
        if (this.editingInitiativeId === removedInitiative.id) {
          this.closeInitiativeModal();
        }
      },
      () => {
        this.config!.iniciativas.splice(index, 0, removedInitiative);
        for (const snapshot of previousDependencias) {
          const target = this.config!.iniciativas.find((item) => item.id === snapshot.id);
          if (target) {
            target.dependencias = snapshot.dependencias;
          }
        }
      }
    );
  }

  saveInitiativeChanges(): void {
    // Bloquear si es CONSULTA
    if (this.isConsulta()) {
      console.log('⛔ CONSULTA no puede editar iniciativas');
      this.error = 'No tienes permisos para editar iniciativas';
      return;
    }

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

    const previousValue = this.config.iniciativas[index];
    this.config.iniciativas[index] = this.normalizeInitiative(JSON.parse(JSON.stringify(this.initiativeDraft)));
    this.persistConfig(
      () => {
        this.saveMessage = 'Iniciativa actualizada correctamente.';
        this.initiativesByAxis = this.groupInitiativesByAxis(this.config!);
        this.closeInitiativeModal();
      },
      () => {
        this.config!.iniciativas[index] = previousValue;
      }
    );
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
   * Rebuilds timeline from selected horizon values.
   */
  onHorizonChange(): void {
    this.rebuildTimeline();
  }

  /**
   * Toggles expedition panel visibility.
   */
  openExpedientesPanel(): void {
    this.activePanel = this.activePanel === 'expedientes' ? 'none' : 'expedientes';
    if (this.activePanel === 'expedientes') {
      this.clampExpedientesPanelWidth();
    }
  }

  /**
   * Toggles commitment panel visibility.
   */
  openCompromisosPanel(): void {
    this.activePanel = this.activePanel === 'compromisos' ? 'none' : 'compromisos';
    if (this.activePanel !== 'compromisos') {
      this.showCommitmentForm = false;
    }
  }

  closeSidePanel(): void {
    this.activePanel = 'none';
    this.showCommitmentForm = false;
  }

  setExpedientesPanelWidth(value: number): void {
    this.expedientesPanelWidth = Number.isFinite(value) ? value : this.expedientesPanelWidth;
    this.clampExpedientesPanelWidth();
  }

  expedientesPanelMaxWidth(): number {
    const viewport = typeof window !== 'undefined' ? window.innerWidth : 1600;
    return Math.max(560, Math.floor(viewport * 0.85));
  }

  /**
   * Returns initiatives containing expedition data.
   */
  expeditionRows(): ExpeditionRow[] {
    if (!this.config) {
      return [];
    }

    const rowsByExpediente = new Map<string, ExpeditionRow>();
    for (const initiative of this.config.iniciativas) {
      const expedientes = this.resolveInitiativeExpedientes(initiative);
      for (let index = 0; index < expedientes.length; index++) {
        const expediente = expedientes[index];
        const row: ExpeditionRow = {
          expedienteId: expediente.id,
          initiativeId: initiative.id,
          initiativeName: initiative.nombre,
          initiativeIds: [initiative.id],
          initiativeNames: [initiative.nombre],
          initiativeExpeditionIndex: index,
          tipo: this.resolveExpedientePrimaryValue(expediente, 'tipo', ['tipo']),
          expediente: this.resolveExpedientePrimaryValue(expediente, 'expediente', ['expediente']),
          impacto: this.resolveExpedientePrimaryValue(expediente, 'impacto', ['impacto', 'impacto_principal']),
          precioLicitacion: this.resolveExpedientePrimaryValue(expediente, 'precio_licitacion', ['precio_licitacion']),
          precioAdjudicacion: this.resolveExpedientePrimaryValue(expediente, 'precio_adjudicacion', ['precio_adjudicacion']),
          empresa: this.resolveExpedientePrimaryValue(expediente, 'empresa', ['empresa']),
          fechaFinExpediente: this.resolveExpedientePrimaryValue(expediente, 'fecha_fin_expediente', ['fecha_fin_expediente']),
          informacionAdicional: { ...(expediente.informacion_adicional || {}) }
        };
        if (!this.hasExpedienteContent(row)) {
          continue;
        }

        const key = this.buildExpeditionAggregationKey(row);
        const existing = rowsByExpediente.get(key);
        if (!existing) {
          rowsByExpediente.set(key, row);
          continue;
        }

        if (!existing.initiativeIds.includes(initiative.id)) {
          existing.initiativeIds.push(initiative.id);
        }
        if (!existing.initiativeNames.includes(initiative.nombre)) {
          existing.initiativeNames.push(initiative.nombre);
        }
      }
    }
    return Array.from(rowsByExpediente.values());
  }

  filteredExpeditionRows(): ExpeditionRow[] {
    const filter = this.expeditionSearch.trim().toLowerCase();
    const rows = this.expeditionRows();
    if (!filter) {
      return rows;
    }
    return rows.filter((row) => {
      const values = [
        row.initiativeId,
        row.initiativeName,
        ...row.initiativeIds,
        ...row.initiativeNames,
        row.tipo,
        row.expediente,
        row.impacto,
        row.precioLicitacion,
        row.precioAdjudicacion,
        row.empresa,
        row.fechaFinExpediente,
        ...Object.entries(row.informacionAdicional || {}).flat()
      ];
      return values.some((value) => String(value || '').toLowerCase().includes(filter));
    });
  }

  expeditionSummary(): ExpeditionSummary {
    const rows = this.filteredExpeditionRows();
    let totalLicitacion = 0;
    let totalAdjudicacion = 0;
    let totalDiscount = 0;
    let discountCount = 0;

    for (const row of rows) {
      const licitacion = this.parseAmount(row.precioLicitacion);
      const adjudicacion = this.parseAmount(row.precioAdjudicacion);
      totalLicitacion += licitacion;
      totalAdjudicacion += adjudicacion;

      const discount = this.calculateDiscount(row.precioLicitacion, row.precioAdjudicacion);
      if (discount !== null) {
        totalDiscount += discount;
        discountCount += 1;
      }
    }

    return {
      totalLicitacion,
      totalAdjudicacion,
      averageDiscount: discountCount > 0 ? totalDiscount / discountCount : 0,
      discountCount
    };
  }

  expeditionDiscountLabel(row: ExpeditionRow): string {
    const discount = this.calculateDiscount(row.precioLicitacion, row.precioAdjudicacion);
    if (discount === null) {
      return '-';
    }
    return `${discount.toFixed(1)}%`;
  }

  initiativesLabel(row: ExpeditionRow): string {
    return (row.initiativeNames || []).join(', ');
  }

  expedientesCatalogoDisponibles(): InitiativeExpediente[] {
    if (!this.config || !this.initiativeDraft) {
      return [];
    }

    const filter = this.linkExpedienteSearch.trim().toLowerCase();
    const linkedIds = new Set(this.draftExpedientes().map((item) => item.id).filter((id) => !!id));
    return (this.config.expedientes_catalogo || [])
      .filter((expediente) => !linkedIds.has(expediente.id))
      .filter((expediente) => {
        if (!filter) {
          return true;
        }
        const values = [
          expediente.expediente,
          expediente.empresa,
          expediente.tipo,
          expediente.precio_licitacion,
          expediente.precio_adjudicacion,
          expediente.fecha_fin_expediente
        ];
        return values.some((value) => String(value || '').toLowerCase().includes(filter));
      });
  }

  toggleLinkExpedienteSelector(): void {
    if (!this.canEdit()) {
      return;
    }
    this.showLinkExpedienteSelector = !this.showLinkExpedienteSelector;
    if (!this.showLinkExpedienteSelector) {
      this.linkExpedienteSearch = '';
    }
  }

  linkExistingExpediente(expediente: InitiativeExpediente): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.initiativeDraft) {
      return;
    }

    const alreadyLinked = this.draftExpedientes().some((item) => item.id === expediente.id);
    if (alreadyLinked) {
      return;
    }

    this.draftExpedientes().push(this.cloneExpediente(expediente));
    this.showLinkExpedienteSelector = false;
    this.linkExpedienteSearch = '';
  }

  /**
   * Opens the initiative modal for one expedition row.
   */
  openExpeditionDetails(row: ExpeditionRow): void {
    if (!this.config) {
      return;
    }
    const availableIds = (row.initiativeIds || []).filter((id) => !!id);
    if (availableIds.length === 0) {
      return;
    }
    this.expeditionContextInitiativeIds = availableIds;
    this.openInitiativeModalById(availableIds[0]);
  }

  expeditionContextInitiatives(): InitiativeConfig[] {
    if (!this.config || this.expeditionContextInitiativeIds.length === 0) {
      return [];
    }
    const byId = new Map(this.config.iniciativas.map((initiative) => [initiative.id, initiative]));
    return this.expeditionContextInitiativeIds
      .map((id) => byId.get(id))
      .filter((initiative): initiative is InitiativeConfig => !!initiative);
  }

  switchExpeditionContextInitiative(initiativeId: string): void {
    this.openInitiativeModalById(initiativeId);
  }

  /**
   * Returns current roadmap commitments.
   */
  commitments(): CommitmentConfig[] {
    return this.config?.compromisos || [];
  }

  filteredCommitments(): CommitmentConfig[] {
    const filter = this.commitmentSearch.trim().toLowerCase();
    const items = this.commitments();
    if (!filter) {
      return items;
    }
    return items.filter((commitment) => {
      const values = [
        commitment.id,
        commitment.descripcion,
        commitment.fecha_comprometido,
        commitment.actor,
        commitment.quien_compromete,
        ...Object.entries(commitment.informacion_adicional || {}).flat()
      ];
      return values.some((value) => String(value || '').toLowerCase().includes(filter));
    });
  }

  /**
   * Opens commitment creation form.
   */
  showAddCommitmentForm(): void {
    if (!this.canEdit()) {
      return;
    }
    this.error = '';
    this.saveMessage = '';
    this.activePanel = 'compromisos';
    this.showCommitmentForm = true;
  }

  /**
   * Opens commitments panel from initiative modal context.
   */
  openCommitmentPanelFromInitiative(): void {
    if (!this.canEdit()) {
      return;
    }
    this.activePanel = 'compromisos';
    this.showAddCommitmentForm();
    this.closeInitiativeModal();
  }

  /**
   * Closes commitment creation form and clears draft.
   */
  cancelCommitmentForm(): void {
    this.showCommitmentForm = false;
    this.commitmentDraft = this.createEmptyCommitmentDraft();
  }

  /**
   * Creates one new commitment and persists the roadmap config.
   */
  addCommitment(): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.config || !this.roadmap?.id) {
      return;
    }
    if (!this.commitmentDraft.descripcion.trim()) {
      this.error = 'La descripción del compromiso es obligatoria.';
      return;
    }

    const newCommitment: CommitmentConfig = {
      ...this.commitmentDraft,
      id: this.commitmentDraft.id.trim() || `COMP-${Date.now()}`,
      descripcion: this.commitmentDraft.descripcion.trim(),
      actor: this.commitmentDraft.actor.trim(),
      quien_compromete: this.commitmentDraft.quien_compromete.trim(),
      informacion_adicional: { ...(this.commitmentDraft.informacion_adicional || {}) }
    };
    this.config.compromisos.push(newCommitment);
    this.persistConfig(
      () => {
        this.saveMessage = 'Compromiso guardado correctamente.';
        this.cancelCommitmentForm();
      },
      () => {
        // rollback local insertion on failure.
        this.config!.compromisos = this.config!.compromisos.filter((item) => item !== newCommitment);
      },
      true
    );
  }

  /**
   * Removes one commitment and persists changes.
   *
   * @param index Position in commitment list.
   */
  removeCommitment(commitment: CommitmentConfig): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.config || !this.roadmap?.id) {
      return;
    }
    const index = this.config.compromisos.indexOf(commitment);
    if (index < 0 || index >= this.config.compromisos.length) {
      return;
    }

    const removed = this.config.compromisos.splice(index, 1)[0];
    this.persistConfig(
      () => {
        this.saveMessage = 'Compromiso eliminado correctamente.';
      },
      () => {
        this.config!.compromisos.splice(index, 0, removed);
      },
      true
    );
  }

  /**
   * Returns custom field keys configured in commitment draft.
   */
  commitmentAdditionalDraftKeys(): string[] {
    return Object.keys(this.commitmentDraft.informacion_adicional || {});
  }

  /**
   * Returns suggested commitment custom keys still not configured in draft.
   */
  availableCommitmentSuggestedKeys(): string[] {
    const existing = new Set(this.commitmentAdditionalDraftKeys());
    return this.commitmentAdditionalKeys.filter((key) => !existing.has(key));
  }

  /**
   * Adds one suggested key to commitment draft custom fields.
   */
  addCommitmentSuggestedKey(key: string): void {
    if (!this.canEdit()) {
      return;
    }
    const normalized = (key || '').trim();
    if (!normalized) {
      return;
    }
    if (!Object.prototype.hasOwnProperty.call(this.commitmentDraft.informacion_adicional, normalized)) {
      this.commitmentDraft.informacion_adicional[normalized] = '';
    }
  }

  /**
   * Adds one new custom field to commitment draft.
   */
  addCommitmentAdditionalField(): void {
    if (!this.canEdit()) {
      return;
    }
    const existing = new Set(Object.keys(this.commitmentDraft.informacion_adicional || {}));
    let candidate = 'nuevo_campo';
    let suffix = 1;
    while (existing.has(candidate)) {
      suffix += 1;
      candidate = `nuevo_campo_${suffix}`;
    }
    this.commitmentDraft.informacion_adicional[candidate] = '';
  }

  /**
   * Renames one custom field key inside commitment draft.
   */
  renameCommitmentAdditionalKey(oldKey: string, newKey: string): void {
    if (!this.canEdit()) {
      return;
    }
    const nextKey = (newKey || '').trim();
    if (!nextKey || nextKey === oldKey) {
      return;
    }
    if (Object.prototype.hasOwnProperty.call(this.commitmentDraft.informacion_adicional, nextKey)) {
      return;
    }
    const value = this.commitmentDraft.informacion_adicional[oldKey];
    delete this.commitmentDraft.informacion_adicional[oldKey];
    this.commitmentDraft.informacion_adicional[nextKey] = value;
  }

  /**
   * Updates one custom field value inside commitment draft.
   */
  updateCommitmentAdditionalValue(key: string, value: string): void {
    if (!this.canEdit()) {
      return;
    }
    this.commitmentDraft.informacion_adicional[key] = value;
  }

  /**
   * Removes one custom field from commitment draft.
   */
  removeCommitmentAdditionalField(key: string): void {
    if (!this.canEdit()) {
      return;
    }
    delete this.commitmentDraft.informacion_adicional[key];
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
    if (!this.canEdit()) {
      return;
    }
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
    if (!this.canEdit()) {
      return;
    }
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
    if (!this.canEdit()) {
      return;
    }
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
    if (!this.canEdit()) {
      return;
    }
    if (!this.initiativeDraft) {
      return;
    }
    if (!this.initiativeDraft.informacion_adicional) {
      this.initiativeDraft.informacion_adicional = {};
    }
    this.initiativeDraft.informacion_adicional[key] = value;
  }

  /**
   * Returns one custom value from current initiative draft.
   */
  getDraftAdditionalValue(key: string): string {
    if (!this.initiativeDraft?.informacion_adicional) {
      return '';
    }
    return this.initiativeDraft.informacion_adicional[key] || '';
  }

  /**
   * Returns expedientes currently configured in initiative draft.
   */
  draftExpedientes(): InitiativeExpediente[] {
    if (!this.initiativeDraft) {
      return [];
    }
    if (!Array.isArray(this.initiativeDraft.expedientes)) {
      this.initiativeDraft.expedientes = [];
    }
    return this.initiativeDraft.expedientes;
  }

  /**
   * Adds one new expediente row in initiative draft.
   */
  addDraftExpediente(): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.initiativeDraft) {
      return;
    }
    this.draftExpedientes().push(this.createEmptyExpediente());
  }

  /**
   * Removes one expediente row from initiative draft.
   */
  removeDraftExpediente(index: number): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.initiativeDraft) {
      return;
    }
    if (index < 0 || index >= this.draftExpedientes().length) {
      return;
    }
    this.initiativeDraft.expedientes.splice(index, 1);
  }

  /**
   * Returns custom keys configured for one expediente row.
   */
  draftExpedienteAdditionalKeys(expediente: InitiativeExpediente): string[] {
    return Object.keys(expediente.informacion_adicional || {});
  }

  /**
   * Returns suggested expediente keys not already represented by first-class
   * fields or custom fields in the current row.
   */
  availableDraftExpedienteSuggestedKeys(expediente: InitiativeExpediente): string[] {
    const reserved = new Set([
      'tipo',
      'empresa',
      'expediente',
      'impacto',
      'precio_licitacion',
      'precio_adjudicacion',
      'fecha_fin_expediente'
    ]);
    const existing = new Set(this.draftExpedienteAdditionalKeys(expediente));
    return this.suggestedAdditionalKeys.filter((key) => !reserved.has(key) && !existing.has(key));
  }

  /**
   * Adds one custom field to an expediente row.
   */
  addDraftExpedienteAdditionalField(expediente: InitiativeExpediente): void {
    if (!this.canEdit()) {
      return;
    }
    if (!expediente.informacion_adicional) {
      expediente.informacion_adicional = {};
    }
    const existing = new Set(Object.keys(expediente.informacion_adicional));
    let candidate = 'nuevo_campo';
    let suffix = 1;
    while (existing.has(candidate)) {
      suffix += 1;
      candidate = `nuevo_campo_${suffix}`;
    }
    expediente.informacion_adicional[candidate] = '';
  }

  /**
   * Adds one suggested custom field to an expediente row.
   */
  addDraftExpedienteSuggestedField(expediente: InitiativeExpediente, key: string): void {
    if (!this.canEdit()) {
      return;
    }
    const normalized = (key || '').trim();
    if (!normalized) {
      return;
    }
    if (!expediente.informacion_adicional) {
      expediente.informacion_adicional = {};
    }
    if (!Object.prototype.hasOwnProperty.call(expediente.informacion_adicional, normalized)) {
      expediente.informacion_adicional[normalized] = '';
    }
  }

  /**
   * Renames one custom key in expediente row.
   */
  renameDraftExpedienteAdditionalKey(expediente: InitiativeExpediente, oldKey: string, newKey: string): void {
    if (!this.canEdit()) {
      return;
    }
    const nextKey = (newKey || '').trim();
    if (!nextKey || nextKey === oldKey) {
      return;
    }
    if (!expediente.informacion_adicional) {
      expediente.informacion_adicional = {};
    }
    if (Object.prototype.hasOwnProperty.call(expediente.informacion_adicional, nextKey)) {
      return;
    }
    const value = expediente.informacion_adicional[oldKey];
    delete expediente.informacion_adicional[oldKey];
    expediente.informacion_adicional[nextKey] = value;
  }

  /**
   * Updates one custom value in expediente row.
   */
  updateDraftExpedienteAdditionalValue(expediente: InitiativeExpediente, key: string, value: string): void {
    if (!this.canEdit()) {
      return;
    }
    if (!expediente.informacion_adicional) {
      expediente.informacion_adicional = {};
    }
    expediente.informacion_adicional[key] = value;
  }

  /**
   * Removes one custom field from expediente row.
   */
  removeDraftExpedienteAdditionalField(expediente: InitiativeExpediente, key: string): void {
    if (!this.canEdit()) {
      return;
    }
    if (!expediente.informacion_adicional) {
      return;
    }
    delete expediente.informacion_adicional[key];
  }

  /**
   * Removes one dynamic field from modal draft.
   */
  removeDraftAdditionalField(key: string): void {
    if (!this.canEdit()) {
      return;
    }
    if (!this.initiativeDraft?.informacion_adicional) {
      return;
    }
    delete this.initiativeDraft.informacion_adicional[key];
  }

  /**
   * Initializes horizon selector from configuration using default 2026-T1 .. 2030-T4 fallback.
   */
  private initializeHorizonSelector(config: RoadmapConfig): void {
    const start = this.parseQuarter(config.horizonte_base?.inicio) || { year: 2026, quarter: 1 };
    const end = this.parseQuarter(config.horizonte_base?.fin) || { year: 2030, quarter: 4 };
    this.selectedStartYear = start.year;
    this.selectedStartQuarter = start.quarter;
    this.selectedEndYear = end.year;
    this.selectedEndQuarter = end.quarter;

    const years = [2026, 2030, start.year, end.year];
    for (const initiative of config.iniciativas || []) {
      const iniStart = this.parseQuarter(initiative.inicio);
      const iniEnd = this.parseQuarter(initiative.fin);
      if (iniStart) {
        years.push(iniStart.year);
      }
      if (iniEnd) {
        years.push(iniEnd.year);
      }
    }

    const minYear = Math.min(...years);
    const maxYear = Math.max(...years);
    this.yearOptions = [];
    for (let year = minYear; year <= maxYear; year++) {
      this.yearOptions.push(year);
    }
  }

  /**
   * Rebuilds timeline and year-groups using currently selected horizon values.
   */
  private rebuildTimeline(): void {
    const start: TimelineQuarter = { year: this.selectedStartYear, quarter: this.selectedStartQuarter };
    const end: TimelineQuarter = { year: this.selectedEndYear, quarter: this.selectedEndQuarter };
    if (this.compareQuarter(start, end) > 0) {
      // Keep selection valid by moving end to start when user selects an inverted range.
      this.selectedEndYear = this.selectedStartYear;
      this.selectedEndQuarter = this.selectedStartQuarter;
    }
    this.timeline = this.buildTimelineFromRange(
      { year: this.selectedStartYear, quarter: this.selectedStartQuarter },
      { year: this.selectedEndYear, quarter: this.selectedEndQuarter }
    );
    this.timelineYearGroups = this.buildYearGroups(this.timeline);
  }

  /**
   * Builds timeline slots between two quarter points, inclusive.
   */
  private buildTimelineFromRange(start: TimelineQuarter, end: TimelineQuarter): TimelineSlot[] {
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
   * Compares two quarter points.
   *
   * @returns negative when a < b, zero when equal, positive when a > b.
   */
  private compareQuarter(a: TimelineQuarter, b: TimelineQuarter): number {
    return (a.year * 10 + a.quarter) - (b.year * 10 + b.quarter);
  }

  /**
   * Persists full roadmap configuration and handles success/error flags.
   *
   * @param onSuccess Callback invoked when persistence succeeds.
   * @param onErrorRollback Callback invoked when persistence fails to undo local optimistic update.
   * @param savingCommitmentOperation Indicates if this save belongs to commitment workflow.
   */
  private persistConfig(
    onSuccess: () => void,
    onErrorRollback: () => void,
    savingCommitmentOperation = false
  ): void {
    if (!this.config || !this.roadmap?.id) {
      return;
    }
    this.error = '';
    if (savingCommitmentOperation) {
      this.savingCommitment = true;
    } else {
      this.savingInitiative = true;
    }

    this.roadmapService.saveConfig(this.roadmap.id, this.config).subscribe({
      next: () => {
        if (savingCommitmentOperation) {
          this.savingCommitment = false;
        } else {
          this.savingInitiative = false;
        }
        onSuccess();
      },
      error: (err) => {
        if (savingCommitmentOperation) {
          this.savingCommitment = false;
        } else {
          this.savingInitiative = false;
        }
        onErrorRollback();
        this.error = err?.error?.message || 'No se pudo guardar la configuración.';
      }
    });
  }

  /**
   * Creates an empty commitment draft used in the "Ver/Crear compromisos" panel.
   */
  private createEmptyCommitmentDraft(): CommitmentConfig {
    return {
      id: '',
      descripcion: '',
      fecha_comprometido: '',
      actor: '',
      quien_compromete: '',
      informacion_adicional: {}
    };
  }

  private createNewInitiativeDraft(axisId: string): InitiativeConfig {
    const defaultStart = this.config?.horizonte_base?.inicio || `${this.selectedStartYear}-T${this.selectedStartQuarter}`;
    const defaultEnd = this.config?.horizonte_base?.fin || `${this.selectedEndYear}-T${this.selectedEndQuarter}`;
    return {
      id: '',
      nombre: '',
      eje: axisId,
      inicio: defaultStart,
      fin: defaultEnd,
      certeza: 'planificado',
      informacion_adicional: {},
      expedientes: [],
      dependencias: []
    };
  }

  private generateInitiativeIdFromName(name: string): string {
    const base = name
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .replace(/[^A-Z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '')
      .slice(0, 28) || 'INI';

    let candidate = base;
    let suffix = 1;
    while (this.config?.iniciativas.some((initiative) => initiative.id === candidate)) {
      suffix += 1;
      candidate = `${base}-${suffix}`;
    }
    return candidate;
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
      iniciativas: (config?.iniciativas || []).map((initiative: any) => this.normalizeInitiative(initiative)),
      expedientes_catalogo: (config?.expedientes_catalogo || []).map((item: any) => this.normalizeSingleExpediente(item)),
      compromisos: (config?.compromisos || []).map((commitment: any) => this.normalizeCommitment(commitment))
    };
  }

  private syncCollapsedAxes(config: RoadmapConfig): void {
    const validAxisIds = new Set((config.ejes_estrategicos || []).map((axis) => String(axis.id || '').trim()).filter((id) => !!id));
    this.collapsedAxisIds.forEach((axisId) => {
      if (!validAxisIds.has(axisId)) {
        this.collapsedAxisIds.delete(axisId);
      }
    });
  }

  private openInitiativeModalById(initiativeId: string): void {
    if (!this.config) {
      return;
    }
    const initiative = this.config.iniciativas.find((item) => item.id === initiativeId);
    if (!initiative) {
      return;
    }

    this.saveMessage = '';
    this.error = '';
    this.editingInitiativeId = initiative.id;
    this.initiativeDraft = this.normalizeInitiative(JSON.parse(JSON.stringify(initiative)));
    this.activeInitiativeTab = 'general';
    this.dependenciesInput = (initiative.dependencias || [])
      .map((d) => d.iniciativa)
      .filter((v) => !!v)
      .join(', ');
    this.showLinkExpedienteSelector = false;
    this.linkExpedienteSearch = '';
    this.initiativeModalOpen = true;
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
   * Produces one commitment object with guaranteed base fields and additional map.
   */
  private normalizeCommitment(raw: any): CommitmentConfig {
    return {
      id: String(raw?.id || ''),
      descripcion: String(raw?.descripcion || ''),
      fecha_comprometido: String(raw?.fecha_comprometido || ''),
      actor: String(raw?.actor || ''),
      quien_compromete: String(raw?.quien_compromete || ''),
      informacion_adicional: this.normalizeAdditionalInfo(raw)
    };
  }

  /**
   * Normalizes initiative expedientes list and keeps compatibility with legacy keys.
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

  /**
   * Returns initiative expedientes, creating compatibility rows when needed.
   */
  resolveInitiativeExpedientes(initiative: InitiativeConfig): InitiativeExpediente[] {
    if (Array.isArray(initiative.expedientes) && initiative.expedientes.length > 0) {
      return initiative.expedientes;
    }
    return this.normalizeExpedientes(initiative);
  }

  /**
   * Resolves one expediente value from first-class field and falls back to
   * additional JSON keys used in legacy/imported payloads.
   */
  private resolveExpedientePrimaryValue(
    expediente: InitiativeExpediente,
    primaryField: keyof InitiativeExpediente,
    fallbackKeys: string[]
  ): string {
    const direct = String(expediente?.[primaryField] ?? '').trim();
    if (direct) {
      return direct;
    }
    const additional = expediente?.informacion_adicional || {};
    for (const key of fallbackKeys) {
      const candidate = String(additional[key] ?? '').trim();
      if (candidate) {
        return candidate;
      }
    }
    return '';
  }

  /**
   * Checks whether one expedition row has displayable information.
   */
  private hasExpedienteContent(row: ExpeditionRow): boolean {
    if (
      row.expediente ||
      row.precioLicitacion ||
      row.precioAdjudicacion ||
      row.empresa ||
      row.fechaFinExpediente ||
      row.tipo ||
      row.impacto
    ) {
      return true;
    }
    return Object.keys(row.informacionAdicional || {}).some((key) => String(row.informacionAdicional[key] || '').trim().length > 0);
  }

  /**
   * Creates one empty expediente draft row.
   */
  private createEmptyExpediente(): InitiativeExpediente {
    return {
      id: `EXP-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      tipo: '',
      empresa: '',
      expediente: '',
      impacto: '',
      precio_licitacion: '',
      precio_adjudicacion: '',
      fecha_fin_expediente: '',
      informacion_adicional: {}
    };
  }

  private buildExpeditionAggregationKey(row: ExpeditionRow): string {
    if (row.expedienteId) {
      return `id:${row.expedienteId}`;
    }
    return [
      row.expediente,
      row.empresa,
      row.tipo,
      row.precioLicitacion,
      row.precioAdjudicacion,
      row.fechaFinExpediente
    ]
      .map((value) => String(value || '').trim().toLowerCase())
      .join('|');
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

  private cloneExpediente(expediente: InitiativeExpediente): InitiativeExpediente {
    return {
      id: String(expediente.id || this.buildExpedienteId(expediente)),
      tipo: String(expediente.tipo || ''),
      empresa: String(expediente.empresa || ''),
      expediente: String(expediente.expediente || ''),
      impacto: String(expediente.impacto || ''),
      precio_licitacion: String(expediente.precio_licitacion || ''),
      precio_adjudicacion: String(expediente.precio_adjudicacion || ''),
      fecha_fin_expediente: String(expediente.fecha_fin_expediente || ''),
      informacion_adicional: { ...(expediente.informacion_adicional || {}) }
    };
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

  private parseAmount(value: string): number {
    const normalized = String(value || '').trim().replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, '');
    const amount = Number(normalized);
    return Number.isFinite(amount) ? amount : 0;
  }

  private calculateDiscount(precioLicitacion: string, precioAdjudicacion: string): number | null {
    const licitacion = this.parseAmount(precioLicitacion);
    const adjudicacion = this.parseAmount(precioAdjudicacion);
    if (!licitacion || licitacion <= 0) {
      return null;
    }
    return ((licitacion - adjudicacion) / licitacion) * 100;
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

  private clampExpedientesPanelWidth(): void {
    const viewport = typeof window !== 'undefined' ? window.innerWidth : 1440;
    const minWidth = 560;
    const maxWidth = Math.max(minWidth, Math.floor(viewport * 0.85));
    this.expedientesPanelWidth = Math.max(minWidth, Math.min(maxWidth, this.expedientesPanelWidth));
  }

  /**
   * Verifica si el usuario puede editar roadmaps.
   */
  canEdit(): boolean {
    const userRole = this.authService.currentUser?.rol;
    return userRole === 'ADMIN' || userRole === 'GESTION';
  }

  /**
   * Verifica si el usuario es solo consulta (lectura).
   */
  isConsulta(): boolean {
    return this.authService.currentUser?.rol === 'CONSULTA';
  }
}
