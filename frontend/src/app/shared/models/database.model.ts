export interface MySQLConfig {
  host: string;
  port: number;
  user: string;
  password: string;
  database: string;
}

export interface OracleConfig {
  host: string;
  port: number;
  user: string;
  password: string;
  sid: string;
}

export interface DatabaseStatus {
  type: string;
  connected: boolean;
  connectionUrl: string;
}

export interface SupportedTypesResponse {
  types: string[];
  description: string;
  mysql: unknown;
  oracle: unknown;
}
