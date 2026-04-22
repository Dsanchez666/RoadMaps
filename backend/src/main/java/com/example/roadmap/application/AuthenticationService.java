package com.example.roadmap.application;

import com.example.roadmap.domain.Sesion;
import com.example.roadmap.domain.SesionRepository;
import com.example.roadmap.domain.Usuario;
import com.example.roadmap.domain.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de autenticación del sistema.
 *
 * Maneja login, primer acceso, logout, y gestión de sesiones.
 * Usa BCrypt para encriptación de contraseñas.
 *
 * @since 1.0
 */
@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(
        UsuarioRepository usuarioRepository,
        SesionRepository sesionRepository,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Autentica un usuario en el login normal.
     *
     * @param username Username del usuario.
     * @param password Contraseña en plano.
     * @return Sesión creada si login es exitoso.
     * @throws AuthenticationException Si las credenciales son inválidas.
     */
    public Sesion login(String username, String password, String ipAddress, String userAgent) throws AuthenticationException {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("Usuario o contraseña incorrectos"));

        if (!usuario.getActivo()) {
            throw new AuthenticationException("Usuario desactivado");
        }

        // Permitir login sin contraseña si es primer acceso (must_change_password = true)
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
            if (!usuario.getMustChangePassword()) {
                throw new AuthenticationException("Usuario sin contraseña. Use endpoint /api/auth/first-password");
            }
            // Si es primer acceso, permitir login sin validar contraseña (password puede ser null/vacía)
        } else {
            // Si tiene contraseña, validarla (pero password no puede ser null)
            if (password == null || password.isEmpty()) {
                throw new AuthenticationException("Usuario o contraseña incorrectos");
            }
            if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
                throw new AuthenticationException("Usuario o contraseña incorrectos");
            }
        }

        // Crear sesión
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getUsername(), usuario.getRol().name());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpirationTime() / 1000);

        Sesion sesion = new Sesion(usuario.getId(), token, expiresAt);
        sesion.setIpAddress(ipAddress);
        sesion.setUserAgent(userAgent);
        sesion.setEstado(Sesion.Estado.ACTIVA);

        return sesionRepository.save(sesion);
    }

    /**
     * Maneja el primer login: usuario establece su contraseña.
     *
     * @param username Username del usuario.
     * @param newPassword Nueva contraseña.
     * @param confirmPassword Confirmación de contraseña.
     * @param ipAddress IP de origen.
     * @param userAgent User-Agent del cliente.
     * @return Sesión creada si el establecimiento es exitoso.
     * @throws AuthenticationException Si hay error en el proceso.
     */
    public Sesion firstPasswordSet(
        String username,
        String newPassword,
        String confirmPassword,
        String ipAddress,
        String userAgent
    ) throws AuthenticationException {

        if (!newPassword.equals(confirmPassword)) {
            throw new AuthenticationException("Las contraseñas no coinciden");
        }

        if (newPassword.length() < 8) {
            throw new AuthenticationException("La contraseña debe tener al menos 8 caracteres");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new AuthenticationException("Usuario desactivado");
        }

        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isEmpty()) {
            throw new AuthenticationException("Este usuario ya tiene contraseña establecida");
        }

        // Encriptar y guardar nueva contraseña
        String encryptedPassword = passwordEncoder.encode(newPassword);
        boolean updated = usuarioRepository.updatePassword(usuario.getId(), encryptedPassword);
        if (!updated) {
            throw new AuthenticationException("No se pudo guardar la nueva contraseña");
        }
        usuario.setPasswordHash(encryptedPassword);
        usuario.setMustChangePassword(false);

        // Crear sesión
        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getUsername(), usuario.getRol().name());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpirationTime() / 1000);

        Sesion sesion = new Sesion(usuario.getId(), token, expiresAt);
        sesion.setIpAddress(ipAddress);
        sesion.setUserAgent(userAgent);
        sesion.setEstado(Sesion.Estado.ACTIVA);

        return sesionRepository.save(sesion);
    }

    /**
     * Realiza logout revocando la sesión.
     *
     * @param token Token de sesión a revocar.
     * @return true si se revocó correctamente.
     */
    public boolean logout(String token) {
        return sesionRepository.revokeByToken(token);
    }

    /**
     * Obtiene la información del usuario desde un token.
     *
     * @param token Token JWT.
     * @return Usuario si el token es válido.
     * @throws AuthenticationException Si el token es inválido.
     */
    public Usuario getUserFromToken(String token) throws AuthenticationException {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("Token inválido o expirado");
        }

        try {
            Integer usuarioId = jwtTokenProvider.getUsuarioIdFromToken(token);
            return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));
        } catch (Exception e) {
            throw new AuthenticationException("Error al validar token: " + e.getMessage());
        }
    }

    /**
     * Valida si una sesión existe y está activa.
     *
     * @param token Token de sesión.
     * @return true si la sesión es válida y activa.
     */
    public boolean isSessionValid(String token) {
        return sesionRepository.isTokenActive(token) && jwtTokenProvider.validateToken(token);
    }

    /**
     * Excepción de autenticación personalizada.
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
