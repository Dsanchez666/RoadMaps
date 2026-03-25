import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ConnectionStateService } from '../../services/connection-state.service';
import { DatabaseStatus, MySQLConfig, OracleConfig } from '../../services/database.service';

/**
 * Component DatabaseConfigComponent
 *
 * Provides a runtime form to connect the backend to MySQL or Oracle and
 * displays current connection status.
 *
 * @selector 'app-database-config'
 * @styleUrls ['./database-config.component.scss']
 */
@Component({
  selector: 'app-database-config',
  standalone: false,
  templateUrl: './database-config.component.html',
  styleUrls: ['./database-config.component.scss']
})
export class DatabaseConfigComponent implements OnInit {
  selectedType: 'mysql' | 'oracle' = 'mysql';
  status: DatabaseStatus | null = null;
  connecting = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  // MySQL config
  mysqlHost = 'localhost';
  mysqlPort = 3306;
  mysqlUser = 'roadmap';
  mysqlPassword = '';
  mysqlDatabase = 'roadmap_mvp';

  // Oracle config
  oracleHost = 'localhost';
  oraclePort = 1521;
  oracleUser = 'system';
  oraclePassword = '';
  oracleSid = 'ORCL';

  constructor(
    private readonly router: Router,
    private readonly connectionState: ConnectionStateService
  ) {}

  /**
   * Loads current connection status when component is initialized.
   */
  ngOnInit(): void {
    this.checkStatus();
  }

  /**
   * Refreshes backend database status in the UI.
   */
  checkStatus(): void {
    this.connectionState.refreshStatus().subscribe({
      next: (status) => {
        this.status = status;
      },
      error: () => {
        this.status = null;
      }
    });
  }

  /**
   * Connects to the selected database engine using form data.
   *
   * Additional Details:
   * The method branches between MySQL and Oracle and updates UI feedback
   * state (`connecting`, `message`, `status`) for both success and error paths.
   */
  connect(): void {
    this.connecting = true;
    this.message = '';

    if (this.selectedType === 'mysql') {
      const config: MySQLConfig = {
        host: this.mysqlHost,
        port: this.mysqlPort,
        user: this.mysqlUser,
        password: this.mysqlPassword,
        database: this.mysqlDatabase
      };
      this.connectionState.connectMySQL(config).subscribe({
        next: () => {
          this.messageType = 'success';
          this.message = 'Conexión a MySQL exitosa';
          this.checkStatus();
          this.connecting = false;
          this.router.navigate(['/roadmaps']);
        },
        error: (err) => {
          this.messageType = 'error';
          this.message = `Error: ${err.error?.message || err.message}`;
          this.connecting = false;
        }
      });
    } else {
      const config: OracleConfig = {
        host: this.oracleHost,
        port: this.oraclePort,
        user: this.oracleUser,
        password: this.oraclePassword,
        sid: this.oracleSid
      };
      this.connectionState.connectOracle(config).subscribe({
        next: () => {
          this.messageType = 'success';
          this.message = 'Conexión a Oracle exitosa';
          this.checkStatus();
          this.connecting = false;
          this.router.navigate(['/roadmaps']);
        },
        error: (err) => {
          this.messageType = 'error';
          this.message = `Error: ${err.error?.message || err.message}`;
          this.connecting = false;
        }
      });
    }
  }

  /**
   * Disconnects current database session and refreshes local state.
   */
  disconnect(): void {
    this.connectionState.disconnect().subscribe({
      next: () => {
        this.messageType = 'success';
        this.message = 'Desconectado';
        this.status = null;
      },
      error: (err) => {
        this.messageType = 'error';
        this.message = `Error: ${err.error?.message || err.message}`;
      }
    });
  }
}
