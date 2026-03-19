import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RoadmapConfig, Initiative } from '../../../../shared/models/roadmap-config.model';

@Component({
  selector: 'app-roadmap-timeline',
  standalone: false,
  templateUrl: './roadmap-timeline.component.html',
  styleUrls: ['./roadmap-timeline.component.scss']
})
export class RoadmapTimelineComponent {
  @Input() data: RoadmapConfig | null = null;
  @Input() allowLoad = true;
  @Output() dataChange = new EventEmitter<RoadmapConfig>();

  jsonFilename = 'roadmap_etna.json';
  legendOpen = false;
  modalOpen = false;
  modalInit: Initiative | null = null;
  activeId: string | null = null;
  depIds: string[] = [];
  closedAxis = new Set<string>();

  get hasData(): boolean {
    return Boolean(this.data && this.data.ejes_estrategicos && this.data.ejes_estrategicos.length);
  }

  isAxisOpen(axisId: string): boolean {
    return !this.closedAxis.has(axisId);
  }

  toggleAxis(axisId: string) {
    if (this.closedAxis.has(axisId)) {
      this.closedAxis.delete(axisId);
    } else {
      this.closedAxis.add(axisId);
    }
  }

  selectJsonFile(input: HTMLInputElement) {
    if (!this.allowLoad) return;
    input.value = '';
    input.click();
  }

  handleJsonFileSelect(file?: File | null) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const parsed = JSON.parse(String(reader.result || '{}')) as RoadmapConfig;
        this.jsonFilename = file.name || 'roadmap_etna.json';
        this.data = parsed;
        this.dataChange.emit(parsed);
      } catch {
        alert('JSON no valido.');
      }
    };
    reader.readAsText(file);
  }

  openLegend() {
    this.legendOpen = true;
  }

  closeLegend() {
    this.legendOpen = false;
  }

  openModal(init: Initiative) {
    this.modalInit = init;
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.modalInit = null;
  }

  onEnter(init: Initiative) {
    this.activeId = init.id;
    this.depIds = this.normalizeDeps(init);
  }

  onLeave() {
    this.activeId = null;
    this.depIds = [];
  }

  isFaded(id: string): boolean {
    if (!this.activeId) return false;
    if (id === this.activeId) return false;
    return !this.depIds.includes(id);
  }

  normalizeDeps(init: Initiative): string[] {
    if (!init.dependencias) return [];
    return init.dependencias
      .map(dep => (dep && typeof dep === 'object' ? dep.iniciativa : ''))
      .filter(Boolean);
  }

  normalizeAxisId(value: string): string {
    if (!value) return '';
    let v = String(value).toUpperCase().trim();
    v = v.replace(/[^A-Z0-9]/g, '');
    if (v.startsWith('EJE')) {
      v = 'E' + v.slice(3);
    }
    return v;
  }

  parseQuarter(value: string, base: string): number | null {
    if (!value) return null;
    const match = value.match(/^(\d{4})-T([1-4])$/);
    if (!match) return null;
    const year = Number(match[1]);
    const quarter = Number(match[2]);
    const baseMatch = (base || '2026-T1').match(/^(\d{4})-T([1-4])$/);
    const baseYear = baseMatch ? Number(baseMatch[1]) : 2026;
    const baseQuarter = baseMatch ? Number(baseMatch[2]) : 1;
    return (year - baseYear) * 4 + (quarter - baseQuarter) + 1;
  }

  toGridRange(init: Initiative): { inicio: number; fin: number } {
    const base = this.data?.horizonte_base?.inicio || '2026-T1';
    const inicio = this.parseQuarter(init.inicio, base) || 1;
    const fin = this.parseQuarter(init.fin, base) || inicio;
    return { inicio, fin };
  }

  axisMatches(init: Initiative, axisId: string): boolean {
    if (init.eje === axisId) return true;
    const norm = this.normalizeAxisId(init.eje);
    return this.normalizeAxisId(axisId) === norm;
  }

  certezaLabel(value: string): string {
    if (!value) return 'N/A';
    return value.charAt(0).toUpperCase() + value.slice(1);
  }
}
