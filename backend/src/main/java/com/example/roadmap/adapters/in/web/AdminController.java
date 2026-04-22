package com.example.roadmap.adapters.in.web;

import com.example.roadmap.adapters.in.web.annotation.RequireRole;
import com.example.roadmap.adapters.in.web.dto.ChangeActivoRequest;
import com.example.roadmap.adapters.in.web.dto.ChangeRolRequest;
import com.example.roadmap.adapters.in.web.dto.CreateUsuarioRequest;
import com.example.roadmap.adapters.in.web.dto.UsuarioResponse;
import com.example.roadmap.domain.Usuario;
import com.example.roadmap.domain.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para administración de usuarios y roles.
 *
 * Solo accesible para usuarios con rol ADMIN.
 *
 * Endpoints:
 * - GET /api/admin/usuarios: Listar todos los usuarios.
 * - POST /api/admin/usuarios: Crear nuevo usuario.
 * - GET /api/admin/usuarios/{id}: Obtener usuario por ID.
 * - PUT /api/admin/usuarios/{id}/rol: Cambiar rol del usuario.
 * - PUT /api/admin/usuarios/{id}/activo: Activar/desactivar usuario.
 * - DELETE /api/admin/usuarios/{id}: Eliminar usuario.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Listar todos los usuarios del sistema.
     * Solo accesible para ADMIN.
     *
     * @return Lista de todos los usuarios.
     */
    @GetMapping("/usuarios")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> listAllUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<UsuarioResponse> response = usuarios.stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al listar usuarios: " + e.getMessage()));
        }
    }

    /**
     * Obtener un usuario por ID.
     * Solo accesible para ADMIN.
     *
     * @param id ID del usuario.
     * @return Información del usuario.
     */
    @GetMapping("/usuarios/{id}")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> getUsuario(@PathVariable Integer id) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado"));
            }

            return ResponseEntity.ok(UsuarioResponse.from(usuario));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al obtener usuario: " + e.getMessage()));
        }
    }

    /**
     * Crear un nuevo usuario.
     * Solo accesible para ADMIN.
     * El usuario se crea sin contraseña, debe usar endpoint /api/auth/first-password
     *
     * @param request CreateUsuarioRequest con username y rol.
     * @return Datos del usuario creado.
     */
    @PostMapping("/usuarios")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> createUsuario(@RequestBody CreateUsuarioRequest request) {
        try {
            // Validaciones
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Username es requerido"));
            }

            if (request.getUsername().length() < 3 || request.getUsername().length() > 100) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Username debe tener entre 3 y 100 caracteres"));
            }

            if (usuarioRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("El username ya existe"));
            }

            if (request.getRol() == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Rol es requerido"));
            }

            // Crear usuario
            Usuario nuevoUsuario = new Usuario(request.getUsername(), null, request.getRol());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                nuevoUsuario.setMustChangePassword(false);
            }
            nuevoUsuario.setCreatedBy("ADMIN");
            nuevoUsuario.setUpdatedBy("ADMIN");

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.from(usuarioGuardado));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al crear usuario: " + e.getMessage()));
        }
    }

    /**
     * Cambiar el rol de un usuario.
     * Solo accesible para ADMIN.
     *
     * @param id ID del usuario.
     * @param request ChangeRolRequest con nuevo rol.
     * @return Datos del usuario actualizado.
     */
    @PutMapping("/usuarios/{id}/rol")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> changeUsuarioRol(
        @PathVariable Integer id,
        @RequestBody ChangeRolRequest request
    ) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado"));
            }

            if (request.getNewRol() == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Nuevo rol es requerido"));
            }

            // No permitir cambiar rol del usuario admin si solo hay uno
            if (usuario.getRol() == Usuario.Role.ADMIN && request.getNewRol() != Usuario.Role.ADMIN) {
                List<Usuario> admins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() == Usuario.Role.ADMIN)
                    .collect(Collectors.toList());

                if (admins.size() <= 1) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("No se puede remover el último usuario ADMIN del sistema"));
                }
            }

            usuarioRepository.updateRol(id, request.getNewRol());
            usuario.setRol(request.getNewRol());
            usuario.setUpdatedBy("ADMIN");

            return ResponseEntity.ok(UsuarioResponse.from(usuario));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al cambiar rol: " + e.getMessage()));
        }
    }

    /**
     * Activar o desactivar un usuario.
     * Solo accesible para ADMIN.
     *
     * @param id ID del usuario.
     * @param request ChangeActivoRequest con nuevo estado.
     * @return Datos del usuario actualizado.
     */
    @PutMapping("/usuarios/{id}/activo")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> changeUsuarioActivo(
        @PathVariable Integer id,
        @RequestBody ChangeActivoRequest request
    ) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado"));
            }

            if (request.getActivo() == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Estado 'activo' es requerido"));
            }

            // No permitir desactivar el último admin
            if (!request.getActivo() && usuario.getRol() == Usuario.Role.ADMIN) {
                List<Usuario> admins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() == Usuario.Role.ADMIN && u.getActivo())
                    .collect(Collectors.toList());

                if (admins.size() <= 1) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("No se puede desactivar el último usuario ADMIN activo del sistema"));
                }
            }

            usuarioRepository.updateActivo(id, request.getActivo());
            usuario.setActivo(request.getActivo());
            usuario.setUpdatedBy("ADMIN");

            return ResponseEntity.ok(UsuarioResponse.from(usuario));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al cambiar estado: " + e.getMessage()));
        }
    }

    /**
     * Eliminar un usuario.
     * Solo accesible para ADMIN.
     * No se puede eliminar el último ADMIN del sistema.
     *
     * @param id ID del usuario a eliminar.
     * @return Mensaje de confirmación.
     */
    @DeleteMapping("/usuarios/{id}")
    @RequireRole(roles = {Usuario.Role.ADMIN})
    public ResponseEntity<Object> deleteUsuario(@PathVariable Integer id) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado"));
            }

            // No permitir eliminar el último admin
            if (usuario.getRol() == Usuario.Role.ADMIN) {
                List<Usuario> admins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() == Usuario.Role.ADMIN)
                    .collect(Collectors.toList());

                if (admins.size() <= 1) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("No se puede eliminar el último usuario ADMIN del sistema"));
                }
            }

            usuarioRepository.delete(id);

            return ResponseEntity.ok(createSuccessResponse("Usuario eliminado correctamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al eliminar usuario: " + e.getMessage()));
        }
    }

    // Utilidades privadas

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
