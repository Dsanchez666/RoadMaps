import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

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

@Injectable({ providedIn: 'root' })
export class DatabaseService {
  private baseUrl = '/api/database';

  constructor(private http: HttpClient) {}

  connectMySQL(config: MySQLConfig): Observable<any> {
    const params = new HttpParams({
      fromObject: {
        host: String(config.host),
        port: String(config.port),
        database: String(config.database),
        user: String(config.user),
        password: String(config.password)
      }
    });
    return this.http.post(`${this.baseUrl}/connect/mysql`, null, { params });
  }

  connectOracle(config: OracleConfig): Observable<any> {
    const params = new HttpParams({
      fromObject: {
        host: String(config.host),
        port: String(config.port),
        sid: String(config.sid),
        user: String(config.user),
        password: String(config.password)
      }
    });
    return this.http.post(`${this.baseUrl}/connect/oracle`, null, { params });
  }

  getStatus(): Observable<DatabaseStatus> {
    return this.http.get<DatabaseStatus>(`${this.baseUrl}/status`);
  }

  disconnect(): Observable<any> {
    return this.http.post(`${this.baseUrl}/disconnect`, {});
  }

  getSupportedTypes(): Observable<SupportedTypesResponse> {
    return this.http.get<SupportedTypesResponse>(`${this.baseUrl}/supported-types`);
  }
}
