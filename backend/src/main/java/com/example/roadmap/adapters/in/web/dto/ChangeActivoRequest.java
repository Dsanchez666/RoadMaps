package com.example.roadmap.adapters.in.web.dto;

/**
 * DTO para activar/desactivar un usuario (solo ADMIN).
 *
 * @since 1.0
 */
public class ChangeActivoRequest {
    private Boolean activo;

    public ChangeActivoRequest() {
    }

    public ChangeActivoRequest(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
