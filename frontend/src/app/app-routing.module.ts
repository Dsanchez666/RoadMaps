import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DatabaseConfigComponent } from './components/database-config/database-config.component';
import { RoadmapEditComponent } from './components/roadmap-edit/roadmap-edit.component';
import { RoadmapListComponent } from './components/roadmap-list/roadmap-list.component';
import { RoadmapViewComponent } from './components/roadmap-view/roadmap-view.component';
import { AuthLoginComponent } from './components/auth-login/auth-login.component';
import { AuthFirstPasswordComponent } from './components/auth-first-password/auth-first-password.component';
import { AdminUsersComponent } from './components/admin-users/admin-users.component';
import { AuthGuard } from './services/auth.guard';
import { RoleGuard } from './services/role.guard';

/**
 * Router configuration for the Angular frontend.
 *
 * Centralizes screen navigation so the UI flow remains explicit and testable.
 */
const routes: Routes = [
  { path: 'login', component: AuthLoginComponent },
  { path: 'first-password', component: AuthFirstPasswordComponent },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'database', component: DatabaseConfigComponent },
  { path: 'roadmaps', component: RoadmapListComponent, canActivate: [AuthGuard] },
  { path: 'roadmaps/:id/view', component: RoadmapViewComponent, canActivate: [AuthGuard] },
  { path: 'roadmaps/:id/edit', component: RoadmapEditComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'GESTION'] } },
  { path: '', pathMatch: 'full', redirectTo: 'database' },
  { path: '**', redirectTo: 'database' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
