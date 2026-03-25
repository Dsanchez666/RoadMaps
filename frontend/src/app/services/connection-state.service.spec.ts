import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ConnectionStateService } from './connection-state.service';
import { DatabaseService } from './database.service';

describe('ConnectionStateService', () => {
  let service: ConnectionStateService;
  let databaseServiceSpy: jasmine.SpyObj<DatabaseService>;

  beforeEach(() => {
    databaseServiceSpy = jasmine.createSpyObj<DatabaseService>('DatabaseService', [
      'connectMySQL',
      'connectOracle',
      'getStatus',
      'disconnect'
    ]);
    databaseServiceSpy.getStatus.and.returnValue(of({ connected: true, type: 'MYSQL', connectionUrl: 'jdbc:mysql://x' }));
    databaseServiceSpy.disconnect.and.returnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        ConnectionStateService,
        { provide: DatabaseService, useValue: databaseServiceSpy }
      ]
    });
    service = TestBed.inject(ConnectionStateService);
  });

  it('should cache mysql credentials in-memory after successful connect', () => {
    databaseServiceSpy.connectMySQL.and.returnValue(of({ status: 'SUCCESS' }));

    service.connectMySQL({
      host: 'localhost',
      port: 3306,
      user: 'roadmap',
      password: 'test-password',
      database: 'roadmap_mvp'
    }).subscribe();

    service.reconnect().subscribe();

    expect(databaseServiceSpy.connectMySQL).toHaveBeenCalledTimes(2);
  });

  it('should clear cached credentials on disconnect', () => {
    databaseServiceSpy.connectMySQL.and.returnValue(of({ status: 'SUCCESS' }));
    service.connectMySQL({
      host: 'localhost',
      port: 3306,
      user: 'roadmap',
      password: 'test-password',
      database: 'roadmap_mvp'
    }).subscribe();
    databaseServiceSpy.connectMySQL.calls.reset();

    service.disconnect().subscribe();

    service.reconnect().subscribe({
      next: () => fail('reconnect should fail after disconnect'),
      error: (err) => {
        expect(err.message).toContain('No hay credenciales previas');
      }
    });
    expect(databaseServiceSpy.connectMySQL).not.toHaveBeenCalled();
  });
});
