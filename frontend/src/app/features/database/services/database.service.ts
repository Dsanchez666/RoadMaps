import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DatabaseStatus, MySQLConfig, OracleConfig, SupportedTypesResponse } from '../../../shared/models/database.model';

@Injectable({ providedIn: 'root' })
export class DatabaseService {
  private baseUrl = '/api/database';

  constructor(private http: HttpClient) {}

  connectMySQL(config: MySQLConfig): Observable<unknown> {
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

  connectOracle(config: OracleConfig): Observable<unknown> {
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

  disconnect(): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/disconnect`, {});
  }

  getSupportedTypes(): Observable<SupportedTypesResponse> {
    return this.http.get<SupportedTypesResponse>(`${this.baseUrl}/supported-types`);
  }
}
