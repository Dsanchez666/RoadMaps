import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

/** MySQL connection payload used by the backend API. */
export interface MySQLConfig {
  host: string;
  port: number;
  user: string;
  password: string;
  database: string;
}

/** Oracle connection payload used by the backend API. */
export interface OracleConfig {
  host: string;
  port: number;
  user: string;
  password: string;
  sid: string;
}

/** Runtime status returned by /api/database/status. */
export interface DatabaseStatus {
  type: string;
  connected: boolean;
  connectionUrl: string;
}

/** Metadata for supported database engines. */
export interface SupportedTypesResponse {
  types: string[];
  description: string;
  mysql: unknown;
  oracle: unknown;
}

/**
 * Service responsible for database connection operations.
 *
 * This service provides methods for connecting/disconnecting and querying
 * backend connectivity status.
 *
 * @providedIn 'root'
 */
@Injectable({ providedIn: 'root' })
export class DatabaseService {
  private baseUrl = '/api/database';

  constructor(private http: HttpClient) {}

  /**
   * Connects the backend to a MySQL database.
   *
   * @param config MySQL connection configuration.
   * @returns Observable<any> API response for connection attempt.
   */
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

  /**
   * Connects the backend to an Oracle database.
   *
   * @param config Oracle connection configuration.
   * @returns Observable<any> API response for connection attempt.
   */
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

  /**
   * Retrieves current backend database status.
   *
   * @returns Observable<DatabaseStatus> Current connectivity state.
   */
  getStatus(): Observable<DatabaseStatus> {
    return this.http.get<DatabaseStatus>(`${this.baseUrl}/status`);
  }

  /**
   * Disconnects current backend database session.
   *
   * @returns Observable<any> API response for disconnection.
   */
  disconnect(): Observable<any> {
    return this.http.post(`${this.baseUrl}/disconnect`, {});
  }

  /**
   * Retrieves metadata for supported database engines.
   *
   * @returns Observable<SupportedTypesResponse> Engine capability information.
   */
  getSupportedTypes(): Observable<SupportedTypesResponse> {
    return this.http.get<SupportedTypesResponse>(`${this.baseUrl}/supported-types`);
  }
}