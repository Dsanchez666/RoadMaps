import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HeaderAction } from '../models/header-action.model';

@Injectable({ providedIn: 'root' })
export class HeaderActionsService {
  private readonly actionsSubject = new BehaviorSubject<HeaderAction[]>([]);
  readonly actions$ = this.actionsSubject.asObservable();

  setActions(actions: HeaderAction[]): void {
    this.actionsSubject.next(actions);
  }

  clear(): void {
    this.actionsSubject.next([]);
  }
}