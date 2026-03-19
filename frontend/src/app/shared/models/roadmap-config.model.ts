export interface RoadmapConfig {
  producto: string;
  organizacion: string;
  horizonte_base: HorizonBase;
  ejes_estrategicos: Axis[];
  iniciativas: Initiative[];
}

export interface HorizonBase {
  inicio: string;
  fin: string;
}

export interface Axis {
  id: string;
  nombre: string;
  descripcion: string;
  color: string;
}

export interface InitiativeDependency {
  iniciativa: string;
  tipo: string;
}

export interface Initiative {
  id: string;
  nombre: string;
  eje: string;
  tipo: string;
  expediente: string;
  inicio: string;
  fin: string;
  objetivo: string;
  impacto_principal: string;
  usuarios_afectados: string;
  dependencias: InitiativeDependency[];
  certeza: string;
}
