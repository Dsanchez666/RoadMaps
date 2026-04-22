package com.example.roadmap.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de Usuarios.
 *
 * @since 1.0
 */
public interface UsuarioRepository {

    /**
     * Busca un usuario por username.
     * @param username El username del usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por ID.
     * @param id El ID del usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findById(Integer id);

    /**
     * Lista todos los usuarios.
     * @return Lista de usuarios.
     */
    List<Usuario> findAll();

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     * @param usuario El usuario a guardar.
     * @return El usuario guardado con ID actualizado.
     */
    Usuario save(Usuario usuario);

    /**
     * Actualiza la contraseña de un usuario.
     * @param usuarioId El ID del usuario.
     * @param passwordHash El nuevo hash de contraseña.
     * @return true si se actualizó correctamente.
     */
    boolean updatePassword(Integer usuarioId, String passwordHash);

    /**
     * Actualiza el rol de un usuario.
     * @param usuarioId El ID del usuario.
     * @param rol El nuevo rol.
     * @return true si se actualizó correctamente.
     */
    boolean updateRol(Integer usuarioId, Usuario.Role rol);

    /**
     * Actualiza el estado activo/inactivo de un usuario.
     * @param usuarioId El ID del usuario.
     * @param activo true para activar, false para desactivar.
     * @return true si se actualizó correctamente.
     */
    boolean updateActivo(Integer usuarioId, Boolean activo);

    /**
     * Elimina un usuario.
     * @param usuarioId El ID del usuario a eliminar.
     * @return true si se eliminó correctamente.
     */
    boolean delete(Integer usuarioId);

    /**
     * Verifica si un username ya existe.
     * @param username El username a verificar.
     * @return true si existe.
     */
    boolean existsByUsername(String username);
}
