package com.example.roadmap.adapters.in.web.dto;

import com.example.roadmap.domain.Usuario;

/**
 * DTO para crear un nuevo usuario (solo ADMIN).
 *
 * @since 1.0
 */
public class CreateUsuarioRequest {
    private String username;
    private String password;
    private Usuario.Role rol;

    public CreateUsuarioRequest() {
    }

    public CreateUsuarioRequest(String username, String password, Usuario.Role rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
