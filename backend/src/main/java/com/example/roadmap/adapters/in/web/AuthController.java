package com.example.roadmap.adapters.in.web;

import com.example.roadmap.adapters.in.web.dto.ChangePasswordRequest;
import com.example.roadmap.adapters.in.web.dto.LoginRequest;
import com.example.roadmap.adapters.in.web.dto.LoginResponse;
import com.example.roadmap.adapters.in.web.dto.SetPasswordRequest;
import com.example.roadmap.adapters.in.web.dto.UsuarioResponse;
import com.example.roadmap.application.AuthenticationService;
import com.example.roadmap.domain.Sesion;
import com.example.roadmap.domain.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación y autorización.
 *
 * Endpoints:
 * - POST /api/auth/login: Login de usuario.
 * - POST /api/auth/first-password: Establecer contraseña en primer acceso.
 * - GET /api/auth/me: Obtener información del usuario autenticado.
 * - POST /api/auth/logout: Cerrar sesión.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Login de usuario con username y password.
     * 
     * @param loginRequest Username y password.
     * @param request HttpServletRequest para obtener IP y User-Agent.
     * @return LoginResponse con token si es exitoso, 401 si falla.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(
        @RequestBody LoginRequest loginRequest,
        HttpServletRequest request
    ) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username requerido"));
            }

            // Nota: La contraseña puede ser null/vacía para primer acceso
            // La validación se hace en AuthenticationService.login()

            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            Sesion sesion = authenticationService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                ipAddress,
                userAgent
            );

            Usuario usuario = getUsuarioFromSesion(sesion);
            LoginResponse response = new LoginResponse(
                sesion.getToken(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getMustChangePassword()
            );

            System.out.println("AuthController.login: Login exitoso para usuario: " + usuario.getUsername());
            return ResponseEntity.ok(response);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error en el servidor: " + e.getMessage()));
        }
    }

    /**
     * Establecer contraseña en primer login.
     * Usuario sin contraseña solo puede usar este endpoint.
     * 
     * @param username Username del usuario.
     * @param setPasswordRequest Nueva contraseña y confirmación.
     * @param request HttpServletRequest para obtener IP y User-Agent.
     * @return LoginResponse con token si es exitoso.
     */
    @PostMapping("/first-password")
    public ResponseEntity<Object> setFirstPassword(
        @RequestBody SetPasswordRequest setPasswordRequest,
        HttpServletRequest request
    ) {
        try {
            if (setPasswordRequest.getUsername() == null || setPasswordRequest.getUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Username requerido"));
            }

            if (setPasswordRequest.getNewPassword() == null || setPasswordRequest.getNewPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Nueva contraseña requerida"));
            }

            if (setPasswordRequest.getConfirmPassword() == null || setPasswordRequest.getConfirmPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Confirmación de contraseña requerida"));
            }

            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            Sesion sesion = authenticationService.firstPasswordSet(
                setPasswordRequest.getUsername(),
                setPasswordRequest.getNewPassword(),
                setPasswordRequest.getConfirmPassword(),
                ipAddress,
                userAgent
            );

            Usuario usuario = getUsuarioFromSesion(sesion);
            LoginResponse response = new LoginResponse(
                sesion.getToken(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                false // Contraseña ya fue establecida
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error en el servidor: " + e.getMessage()));
        }
    }

    /**
     * Obtener información del usuario autenticado.
     * Requiere token en cabecera Authorization: Bearer <token>
     * 
     * @param authHeader Cabecera Authorization.
     * @return UsuarioResponse con información del usuario.
     */
    @GetMapping("/me")
    public ResponseEntity<Object> getMe(
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token no proporcionado"));
            }

            Usuario usuario = authenticationService.getUserFromToken(token);
            UsuarioResponse response = UsuarioResponse.from(usuario);

            return ResponseEntity.ok(response);

        } catch (AuthenticationService.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error en el servidor: " + e.getMessage()));
        }
    }

    /**
     * Cerrar sesión revocando el token.
     * 
     * @param authHeader Cabecera Authorization.
     * @return Respuesta exitosa si se revoca correctamente.
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token no proporcionado"));
            }

            boolean revoked = authenticationService.logout(token);
            if (revoked) {
                return ResponseEntity.ok(createSuccessResponse("Sesión cerrada correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("No se pudo revocar la sesión"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error en el servidor: " + e.getMessage()));
        }
    }

    // Utilidades privadas

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private Usuario getUsuarioFromSesion(Sesion sesion) throws AuthenticationService.AuthenticationException {
        // Extrae el usuarioId de la sesión y obtiene el usuario
        return authenticationService.getUserFromToken(sesion.getToken());
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("message", message);
        return response;
    }
}
