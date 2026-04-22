package com.example.roadmap.adapters.in.web.annotation;

import com.example.roadmap.domain.Usuario;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para proteger endpoints por rol.
 *
 * Se aplica a métodos de controladores para restringir acceso a ciertos roles.
 * Requiere que el usuario esté autenticado y tenga uno de los roles especificados.
 *
 * Ejemplo:
 * @RequireRole(roles = {Role.ADMIN})
 * @PostMapping("/users")
 * public ResponseEntity<?> createUser(...) { ... }
 *
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * Roles requeridos para acceder al endpoint.
     * Si el usuario tiene al menos uno de estos roles, puede acceder.
     */
    Usuario.Role[] roles() default {Usuario.Role.CONSULTA, Usuario.Role.GESTION, Usuario.Role.ADMIN};

    /**
     * Si true, requiere que el usuario esté autenticado pero no valida rol específico.
     */
    boolean requireAuth() default true;
}
