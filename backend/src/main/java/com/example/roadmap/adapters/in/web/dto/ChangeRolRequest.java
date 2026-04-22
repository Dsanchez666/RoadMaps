package com.example.roadmap.adapters.in.web.dto;

import com.example.roadmap.domain.Usuario;

/**
 * DTO para cambiar el rol de un usuario (solo ADMIN).
 *
 * @since 1.0
 */
public class ChangeRolRequest {
    private Usuario.Role newRol;

    public ChangeRolRequest() {
    }

    public ChangeRolRequest(Usuario.Role newRol) {
        this.newRol = newRol;
    }

    public Usuario.Role getNewRol() {
        return newRol;
    }

    public void setNewRol(Usuario.Role newRol) {
        this.newRol = newRol;
    }
}
