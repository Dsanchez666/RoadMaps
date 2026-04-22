import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConnectionStateService } from '../../services/connection-state.service';

@Component({
  standalone: true,
  selector: 'app-auth-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './auth-login.component.html',
  styleUrls: ['./auth-login.component.scss']
})
export class AuthLoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private readonly authService: AuthService,
    private readonly connectionState: ConnectionStateService,
    private readonly router: Router
  ) {}

  login(): void {
    this.error = '';
    this.loading = true;
    console.log('');
    console.log('🔐 [LOGIN] Iniciando proceso de login...');
    console.log(`   👤 Usuario: ${this.username}`);
    
    this.authService.login({ username: this.username.trim(), password: this.password })
      .subscribe({
        next: (response) => {
          this.loading = false;
          console.log('   ✅ Login exitoso');
          console.log(`   🔑 Token recibido`);
          console.log(`   👤 Usuario: ${response.username}`);
          console.log(`   👑 Rol: ${response.rol}`);
          console.log(`   🔄 ¿Cambiar contraseña? ${response.mustChangePassword}`);
          console.log('');

          if (response.mustChangePassword) {
            console.log('📌 PASO 1: Cambiar contraseña (primer acceso)');
            console.log('   ➡️  NAVEGANDO A: /first-password');
            this.router.navigate(['/first-password']);
            return;
          }

          console.log('📌 PASO 1: Verificar conexión a BD');
          // PRIMERO: Verificar conexión a BD
          const isConnected = this.connectionState.statusSnapshot !== null;
          
          if (!isConnected) {
            console.log('   ❌ NO hay conexión a BD');
            console.log('   ➡️  NAVEGANDO A: /database');
            console.log('   📝 Razón: PRIMERO hay que configurar BD, luego ir a aplicación');
            this.router.navigate(['/database']);
          } else {
            console.log('   ✅ SÍ hay conexión a BD');
            console.log('');
            console.log('📌 PASO 2: Verificar rol del usuario');
            // SÍ hay conexión → redirigir según rol
            const currentUser = this.authService.currentUser;
            if (currentUser?.rol === 'ADMIN') {
              console.log('   👑 ADMIN');
              console.log('   ➡️  NAVEGANDO A: /admin/users');
              this.router.navigate(['/admin/users']);
            } else {
              console.log(`   📊 ${currentUser?.rol}`);
              console.log('   ➡️  NAVEGANDO A: /roadmaps');
              this.router.navigate(['/roadmaps']);
            }
          }
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || err?.message || 'Error de autenticación';
          console.log('   ❌ Error en login');
          console.log(`   📝 ${this.error}`);
        }
      });
  }
}
