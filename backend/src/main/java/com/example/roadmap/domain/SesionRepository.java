package com.example.roadmap.domain;

import java.util.Optional;
import java.util.List;

/**
 * Repositorio para acceso a datos de Sesiones.
 *
 * @since 1.0
 */
public interface SesionRepository {

    /**
     * Busca una sesión por token.
     * @param token El token de sesión.
     * @return Optional con la sesión si existe.
     */
    Optional<Sesion> findByToken(String token);

    /**
     * Busca una sesión por ID.
     * @param id El ID de la sesión.
     * @return Optional con la sesión si existe.
     */
    Optional<Sesion> findById(Integer id);

    /**
     * Lista todas las sesiones activas de un usuario.
     * @param usuarioId El ID del usuario.
     * @return Lista de sesiones activas del usuario.
     */
    List<Sesion> findActiveByUsuarioId(Integer usuarioId);

    /**
     * Guarda una nueva sesión.
     * @param sesion La sesión a guardar.
     * @return La sesión guardada con ID actualizado.
     */
    Sesion save(Sesion sesion);

    /**
     * Actualiza el estado de una sesión.
     * @param sesionId El ID de la sesión.
     * @param estado El nuevo estado.
     * @return true si se actualizó correctamente.
     */
    boolean updateEstado(Integer sesionId, Sesion.Estado estado);

    /**
     * Revoca una sesión por token.
     * @param token El token de la sesión a revocar.
     * @return true si se revocó correctamente.
     */
    boolean revokeByToken(String token);

    /**
     * Revoca todas las sesiones de un usuario.
     * @param usuarioId El ID del usuario.
     * @return Número de sesiones revocadas.
     */
    int revokeAllByUsuarioId(Integer usuarioId);

    /**
     * Elimina sesiones expiradas.
     * @return Número de sesiones eliminadas.
     */
    int deleteExpired();

    /**
     * Verifica si un token existe y está activo.
     * @param token El token a verificar.
     * @return true si el token existe y está activo.
     */
    boolean isTokenActive(String token);
}
