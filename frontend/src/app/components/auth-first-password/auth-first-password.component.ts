import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-auth-first-password',
  imports: [CommonModule, FormsModule],
  templateUrl: './auth-first-password.component.html',
  styleUrls: ['./auth-first-password.component.scss']
})
export class AuthFirstPasswordComponent {
  username = '';
  newPassword = '';
  confirmPassword = '';
  error = '';
  loading = false;

  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  submit(): void {
    this.error = '';
    this.loading = true;
    this.authService.setFirstPassword({
      username: this.username.trim(),
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/roadmaps']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'No se pudo establecer la contraseña';
      }
    });
  }
}
