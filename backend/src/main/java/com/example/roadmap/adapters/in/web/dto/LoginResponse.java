package com.example.roadmap.adapters.in.web.dto;

import com.example.roadmap.domain.Usuario;

/**
 * DTO para respuesta de login exitoso.
 *
 * Retorna el token de sesión, información del usuario y si debe cambiar contraseña.
 *
 * @since 1.0
 */
public class LoginResponse {
    private String token;
    private Integer usuarioId;
    private String username;
    private Usuario.Role rol;
    private Boolean mustChangePassword;

    public LoginResponse() {
    }

    public LoginResponse(String token, Integer usuarioId, String username, Usuario.Role rol, Boolean mustChangePassword) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.username = username;
        this.rol = rol;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
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

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
