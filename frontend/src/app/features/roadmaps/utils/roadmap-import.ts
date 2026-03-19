import { Roadmap } from '../../../shared/models/roadmap.model';

export interface RoadmapImportResult {
  roadmaps: Roadmap[];
  preview: any | null;
  error?: string;
}

export function buildImportResult(raw: any): RoadmapImportResult {
  const items = extractItems(raw);
  if (!Array.isArray(items)) {
    return {
      roadmaps: [],
      preview: null,
      error: 'Formato JSON no valido. Se esperaba un array o { roadmaps: [...] }.'
    };
  }

  const preview = pickPreview(items);
  const roadmaps = toRoadmaps(items);

  if (roadmaps.length === 0) {
    return { roadmaps: [], preview, error: 'No hay roadmaps validos para cargar.' };
  }

  return { roadmaps, preview };
}

function extractItems(raw: any): any[] | null {
  if (!Array.isArray(raw) && raw && typeof raw === 'object' && typeof raw.title === 'string') {
    return [raw];
  }
  if (Array.isArray(raw)) {
    return raw;
  }
  if (Array.isArray(raw?.roadmaps)) {
    return raw.roadmaps;
  }
  return null;
}

function pickPreview(items: any[]): any | null {
  if (items.length === 0) {
    return null;
  }
  return (
    items
      .filter((r: any) => r && typeof r === 'object')
      .sort((a: any, b: any) => scoreRoadmap(b) - scoreRoadmap(a))[0] || null
  );
}

function scoreRoadmap(r: any): number {
  const ejes = Array.isArray(r?.ejes_estrategicos) ? r.ejes_estrategicos.length : 0;
  const iniciativas = Array.isArray(r?.iniciativas) ? r.iniciativas.length : 0;
  const hasDescription = String(r?.description || '').trim().length > 0 ? 1 : 0;
  return (ejes * 10) + (iniciativas * 10) + hasDescription;
}

function toRoadmaps(items: any[]): Roadmap[] {
  const unique = new Set<string>();
  return items
    .map((r: any) => ({
      title: String(r?.title || '').trim(),
      description: String(r?.description || '').trim()
    }))
    .filter((r: Roadmap) => {
      if (r.title.length === 0) {
        return false;
      }
      const key = `${r.title}::${r.description}`;
      if (unique.has(key)) {
        return false;
      }
      unique.add(key);
      return true;
    });
}
