import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { DatabaseService } from '../../services/database.service';
import { DatabaseStatus, MySQLConfig, OracleConfig } from '../../../../shared/models/database.model';

@Component({
  selector: 'app-database-config',
  standalone: false,
  templateUrl: './database-config.component.html',
  styleUrls: ['./database-config.component.scss']
})
export class DatabaseConfigComponent implements OnInit {
  @Output() connectedChange = new EventEmitter<boolean>();

  selectedType: 'mysql' | 'oracle' = 'mysql';
  status: DatabaseStatus | null = null;
  connecting = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  mysqlHost = 'localhost';
  mysqlPort = 3306;
  mysqlUser = 'root';
  mysqlPassword = '';
  mysqlDatabase = 'roadmap_mvp';

  oracleHost = 'localhost';
  oraclePort = 1521;
  oracleUser = 'system';
  oraclePassword = '';
  oracleSid = 'ORCL';

  constructor(private databaseService: DatabaseService) {}

  ngOnInit(): void {
    this.checkStatus();
  }

  checkStatus(): void {
    this.databaseService.getStatus().subscribe({
      next: (status) => {
        this.status = status;
        this.connectedChange.emit(Boolean(status?.connected));
      },
      error: () => {
        this.status = null;
        this.connectedChange.emit(false);
      }
    });
  }

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
      this.databaseService.connectMySQL(config).subscribe({
        next: () => {
          this.messageType = 'success';
          this.message = 'Conexión a MySQL exitosa';
          this.checkStatus();
          this.connecting = false;
          this.connectedChange.emit(true);
        },
        error: (err) => {
          this.messageType = 'error';
          this.message = `Error: ${err.error?.message || err.message}`;
          this.connecting = false;
          this.connectedChange.emit(false);
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
      this.databaseService.connectOracle(config).subscribe({
        next: () => {
          this.messageType = 'success';
          this.message = 'Conexión a Oracle exitosa';
          this.checkStatus();
          this.connecting = false;
          this.connectedChange.emit(true);
        },
        error: (err) => {
          this.messageType = 'error';
          this.message = `Error: ${err.error?.message || err.message}`;
          this.connecting = false;
          this.connectedChange.emit(false);
        }
      });
    }
  }

  disconnect(): void {
    this.databaseService.disconnect().subscribe({
      next: () => {
        this.messageType = 'success';
        this.message = 'Desconectado';
        this.status = null;
        this.connectedChange.emit(false);
      },
      error: (err) => {
        this.messageType = 'error';
        this.message = `Error: ${err.error?.message || err.message}`;
        this.connectedChange.emit(false);
      }
    });
  }
}
