import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

export interface Usuario {
  usuarioId: number;
  username: string;
  rol: 'ADMIN' | 'GESTION' | 'CONSULTA';
  mustChangePassword: boolean;
  createdAt: string;
  updatedAt: string;
}

@Component({
  standalone: true,
  selector: 'app-admin-users',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  usuarios: Usuario[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingUser: Usuario | null = null;

  newUser = {
    username: '',
    password: '',
    rol: 'CONSULTA' as 'ADMIN' | 'GESTION' | 'CONSULTA'
  };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    this.http.get<Usuario[]>('/api/admin/usuarios').subscribe({
      next: (users) => {
        this.usuarios = users;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al cargar usuarios';
        this.loading = false;
      }
    });
  }

  createUser(): void {
    this.loading = true;
    this.error = '';
    this.http.post<Usuario>('/api/admin/usuarios', this.newUser).subscribe({
      next: (user) => {
        this.usuarios.push(user);
        this.showCreateForm = false;
        this.newUser = { username: '', password: '', rol: 'CONSULTA' };
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al crear usuario';
        this.loading = false;
      }
    });
  }

  updateUser(user: Usuario): void {
    this.loading = true;
    this.error = '';
    this.http.put<Usuario>(`/api/admin/usuarios/${user.usuarioId}`, {
      username: user.username,
      rol: user.rol
    }).subscribe({
      next: (updatedUser) => {
        const index = this.usuarios.findIndex(u => u.usuarioId === updatedUser.usuarioId);
        if (index !== -1) {
          this.usuarios[index] = updatedUser;
        }
        this.editingUser = null;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al actualizar usuario';
        this.loading = false;
      }
    });
  }

  deleteUser(userId: number): void {
    if (!confirm('¿Está seguro de que desea eliminar este usuario?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.http.delete(`/api/admin/usuarios/${userId}`).subscribe({
      next: () => {
        this.usuarios = this.usuarios.filter(u => u.usuarioId !== userId);
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al eliminar usuario';
        this.loading = false;
      }
    });
  }

  startEdit(user: Usuario): void {
    this.editingUser = { ...user };
  }

  cancelEdit(): void {
    this.editingUser = null;
  }

  resetPassword(userId: number): void {
    if (!confirm('¿Está seguro de que desea resetear la contraseña de este usuario?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.http.post(`/api/admin/usuarios/${userId}/reset-password`, {}).subscribe({
      next: () => {
        this.loadUsers(); // Reload to see updated mustChangePassword
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al resetear contraseña';
        this.loading = false;
      }
    });
  }
}