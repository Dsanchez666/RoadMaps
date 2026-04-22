package com.example.roadmap.adapters.in.web.interceptor;

import com.example.roadmap.application.AuthenticationService;
import com.example.roadmap.domain.Usuario;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro JWT que valida tokens en cada solicitud.
 *
 * El token debe venir en la cabecera Authorization: Bearer <token>
 * Si es válido, se agrega la información del usuario al request como atributo.
 *
 * @since 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    public JwtAuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        try {
            String token = extractToken(request);

            if (token != null) {
                if (authenticationService.isSessionValid(token)) {
                    Usuario usuario = authenticationService.getUserFromToken(token);
                    request.setAttribute("currentUser", usuario);
                    request.setAttribute("currentToken", token);
                    logger.debug("JWT válido para usuario: " + usuario.getUsername() + " en " + requestPath);
                } else {
                    logger.warn("Token JWT inválido o expirado en " + requestPath);
                }
            } else {
                logger.debug("No se encontró token en Authorization header para " + requestPath);
            }
        } catch (Exception e) {
            // Token inválido o expirado, continuar sin usuario
            logger.warn("Error validando token JWT en " + requestPath + ": " + e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
