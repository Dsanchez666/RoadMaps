import { of } from 'rxjs';

import { RoadmapCreateComponent } from './roadmap-create.component';
import { RoadmapService } from '../../services/roadmap.service';

describe('RoadmapCreateComponent', () => {
  let serviceSpy: jasmine.SpyObj<RoadmapService>;
  let component: RoadmapCreateComponent;

  beforeEach(() => {
    serviceSpy = jasmine.createSpyObj<RoadmapService>('RoadmapService', [
      'create',
      'importRoadmap',
      'notifyRoadmapsChanged'
    ]);
    serviceSpy.create.and.returnValue(of({ id: 'rm-1', title: 'T1', description: '' }));
    serviceSpy.importRoadmap.and.returnValue(of({ id: 'rm-2', title: 'T2', description: '' }));
    component = new RoadmapCreateComponent(serviceSpy);
  });

  it('should create roadmap and notify list refresh', () => {
    component.title = 'Nuevo';
    component.description = 'Desc';

    component.save();

    expect(serviceSpy.create).toHaveBeenCalled();
    expect(serviceSpy.notifyRoadmapsChanged).toHaveBeenCalled();
  });

  it('should discard pending import and notify list refresh', () => {
    component.pendingImports = [{ title: 'A' }];
    component.preview = { title: 'A' };
    component.importError = 'x';
    component.importedCount = 2;

    component.descartarImportacion();

    expect(component.pendingImports.length).toBe(0);
    expect(component.preview).toBeNull();
    expect(component.importError).toBe('');
    expect(component.importedCount).toBe(0);
    expect(serviceSpy.notifyRoadmapsChanged).toHaveBeenCalled();
  });
});
