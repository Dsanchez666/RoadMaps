import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { DatabaseStatus, MySQLConfig, OracleConfig, SupportedTypesResponse } from '../../../shared/models/database.model';

const LAST_DB_TYPE_KEY = 'roadmaps.last.db.type';
const LAST_MYSQL_CONFIG_KEY = 'roadmaps.last.mysql.config';
const LAST_ORACLE_CONFIG_KEY = 'roadmaps.last.oracle.config';

@Injectable({ providedIn: 'root' })
export class DatabaseService {
  private baseUrl = '/api/database';
  private readonly statusSubject = new BehaviorSubject<DatabaseStatus | null>(null);
  readonly status$ = this.statusSubject.asObservable();

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

    this.saveLastMySqlConfig(config);

    return this.http.post(`${this.baseUrl}/connect/mysql`, null, { params })
      .pipe(tap(() => this.statusSubject.next({
        type: 'mysql',
        connected: true,
        connectionUrl: ''
      })));
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

    this.saveLastOracleConfig(config);

    return this.http.post(`${this.baseUrl}/connect/oracle`, null, { params })
      .pipe(tap(() => this.statusSubject.next({
        type: 'oracle',
        connected: true,
        connectionUrl: ''
      })));
  }

  getStatus(): Observable<DatabaseStatus> {
    return this.http.get<DatabaseStatus>(`${this.baseUrl}/status`)
      .pipe(tap((status) => this.statusSubject.next(status)));
  }

  disconnect(): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/disconnect`, {})
      .pipe(tap(() => this.statusSubject.next({
        type: '',
        connected: false,
        connectionUrl: ''
      })));
  }

  getSupportedTypes(): Observable<SupportedTypesResponse> {
    return this.http.get<SupportedTypesResponse>(`${this.baseUrl}/supported-types`);
  }

  reconnectLast(): Observable<DatabaseStatus> {
    const type = localStorage.getItem(LAST_DB_TYPE_KEY);

    if (type === 'mysql') {
      const cfg = this.readLastMySqlConfig();
      if (!cfg) {
        return throwError(() => new Error('No hay configuracion MySQL previa para reconectar.'));
      }
      return this.connectMySQL(cfg).pipe(switchMap(() => this.getStatus()));
    }

    if (type === 'oracle') {
      const cfg = this.readLastOracleConfig();
      if (!cfg) {
        return throwError(() => new Error('No hay configuracion Oracle previa para reconectar.'));
      }
      return this.connectOracle(cfg).pipe(switchMap(() => this.getStatus()));
    }

    return throwError(() => new Error('No existe una configuracion de conexion previa.'));
  }

  private saveLastMySqlConfig(config: MySQLConfig): void {
    localStorage.setItem(LAST_DB_TYPE_KEY, 'mysql');
    localStorage.setItem(LAST_MYSQL_CONFIG_KEY, JSON.stringify(config));
  }

  private saveLastOracleConfig(config: OracleConfig): void {
    localStorage.setItem(LAST_DB_TYPE_KEY, 'oracle');
    localStorage.setItem(LAST_ORACLE_CONFIG_KEY, JSON.stringify(config));
  }

  private readLastMySqlConfig(): MySQLConfig | null {
    const raw = localStorage.getItem(LAST_MYSQL_CONFIG_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as MySQLConfig;
    } catch {
      return null;
    }
  }

  private readLastOracleConfig(): OracleConfig | null {
    const raw = localStorage.getItem(LAST_ORACLE_CONFIG_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as OracleConfig;
    } catch {
      return null;
    }
  }
}