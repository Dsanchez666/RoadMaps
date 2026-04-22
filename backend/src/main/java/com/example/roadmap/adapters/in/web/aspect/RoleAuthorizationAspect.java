package com.example.roadmap.adapters.in.web.aspect;

import com.example.roadmap.adapters.in.web.annotation.RequireRole;
import com.example.roadmap.domain.Usuario;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto AOP para validar la anotación @RequireRole.
 *
 * Intercepta métodos anotados con @RequireRole y verifica que el usuario
 * esté autenticado y tenga los roles requeridos.
 *
 * @since 1.0
 */
@Aspect
@Component
public class RoleAuthorizationAspect {
    private static final Logger LOG = LoggerFactory.getLogger(RoleAuthorizationAspect.class);

    /**
     * Valida autorización por rol.
     */
    @Around("@annotation(requireRole)")
    public Object validateRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        HttpServletRequest request = getRequest();

        if (request == null) {
            LOG.warn("No request context available for {}", joinPoint.getSignature().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("No request context available"));
        }

        String requestPath = request.getRequestURI();
        Usuario currentUser = (Usuario) request.getAttribute("currentUser");

        LOG.debug("RoleAuthorizationAspect - Validando acceso a {}, usuario: {}", 
            requestPath, currentUser != null ? currentUser.getUsername() : "NO AUTENTICADO");

        // Verificar si el usuario está autenticado
        if (currentUser == null) {
            if (requireRole.requireAuth()) {
                LOG.warn("Acceso denegado a {} - Autenticación requerida", requestPath);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Autenticación requerida"));
            }
        } else {
            // Verificar si el usuario tiene uno de los roles requeridos
            Usuario.Role[] requiredRoles = requireRole.roles();
            boolean hasRole = false;

            for (Usuario.Role role : requiredRoles) {
                if (currentUser.getRol() == role) {
                    hasRole = true;
                    break;
                }
            }

            if (!hasRole) {
                LOG.warn("Acceso denegado a {} - Usuario {} no tiene rol requerido (tiene: {})", 
                    requestPath, currentUser.getUsername(), currentUser.getRol());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("No tienes permisos para acceder a este recurso"));
            }
            
            LOG.debug("Acceso permitido a {} - Usuario {} con rol {}", 
                requestPath, currentUser.getUsername(), currentUser.getRol());
        }

        // El usuario tiene autorización, proceder
        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        return response;
    }
}
