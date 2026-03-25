import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { DatabaseService, DatabaseStatus, MySQLConfig, OracleConfig } from './database.service';

type StoredConnection =
  | { type: 'mysql'; config: MySQLConfig }
  | { type: 'oracle'; config: OracleConfig };

/**
 * ConnectionStateService
 *
 * Holds runtime DB connection state for the Angular app and provides a
 * reconnect operation based on the last successful credentials.
 *
 * Security note:
 * credentials are never persisted to browser storage and are only kept
 * in-memory for the current tab lifecycle.
 */
@Injectable({ providedIn: 'root' })
export class ConnectionStateService {
  private readonly statusSubject = new BehaviorSubject<DatabaseStatus | null>(null);
  readonly status$ = this.statusSubject.asObservable();

  private lastSuccessfulConnection: StoredConnection | null = null;

  constructor(private readonly databaseService: DatabaseService) {}

  /**
   * Returns latest cached status snapshot.
   */
  get statusSnapshot(): DatabaseStatus | null {
    return this.statusSubject.value;
  }

  /**
   * Checks backend connection status and updates local observable state.
   */
  refreshStatus(): Observable<DatabaseStatus | null> {
    return this.databaseService.getStatus().pipe(
      tap((status) => this.statusSubject.next(status)),
      map((status) => status as DatabaseStatus | null),
      catchError(() => {
        this.statusSubject.next({ connected: false, type: '', connectionUrl: '' });
        return throwError(() => new Error('No se pudo consultar el estado de conexión'));
      })
    );
  }

  /**
   * Connects to MySQL and stores credentials for reconnect.
   */
  connectMySQL(config: MySQLConfig): Observable<any> {
    return this.databaseService.connectMySQL(config).pipe(
      tap(() => {
        // Security: keep credentials only in-memory for this browser tab lifetime.
        this.lastSuccessfulConnection = { type: 'mysql', config: { ...config } };
      }),
      tap(() => {
        this.refreshStatus().subscribe({ error: () => void 0 });
      })
    );
  }

  /**
   * Connects to Oracle and stores credentials for reconnect.
   */
  connectOracle(config: OracleConfig): Observable<any> {
    return this.databaseService.connectOracle(config).pipe(
      tap(() => {
        // Security: keep credentials only in-memory for this browser tab lifetime.
        this.lastSuccessfulConnection = { type: 'oracle', config: { ...config } };
      }),
      tap(() => {
        this.refreshStatus().subscribe({ error: () => void 0 });
      })
    );
  }

  /**
   * Disconnects and clears cached runtime status.
   */
  disconnect(): Observable<any> {
    return this.databaseService.disconnect().pipe(
      tap(() => {
        this.statusSubject.next({ connected: false, type: '', connectionUrl: '' });
        this.lastSuccessfulConnection = null;
      })
    );
  }

  /**
   * Reconnects using the last successful connection credentials.
   */
  reconnect(): Observable<any> {
    if (!this.lastSuccessfulConnection) {
      return throwError(() => new Error('No hay credenciales previas para reconectar'));
    }

    if (this.lastSuccessfulConnection.type === 'mysql') {
      return this.connectMySQL(this.lastSuccessfulConnection.config);
    }
    return this.connectOracle(this.lastSuccessfulConnection.config);
  }
}
