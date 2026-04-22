package com.example.roadmap.adapters.in.web.dto;

import com.example.roadmap.domain.Usuario;

/**
 * DTO para retornar información del usuario autenticado.
 *
 * Usado en el endpoint GET /api/auth/me
 *
 * @since 1.0
 */
public class UsuarioResponse {
    private Integer id;
    private String username;
    private Usuario.Role rol;
    private Boolean activo;
    private Boolean mustChangePassword;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Integer id, String username, Usuario.Role rol, Boolean activo, Boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.rol = rol;
        this.activo = activo;
        this.mustChangePassword = mustChangePassword;
    }

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getRol(),
            usuario.getActivo(),
            usuario.getMustChangePassword()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Usuario.Role getRol() {
        return rol;
    }

    public void setRol(Usuario.Role rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
