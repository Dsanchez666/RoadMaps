package com.example.roadmap.domain;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Sesión de usuario.
 *
 * Almacena información de sesiones activas con tokens JWT.
 * Permite auditoría y revocación de sesiones.
 *
 * @since 1.0
 */
public class Sesion {
    private Integer id;
    private Integer usuarioId;
    private String token;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Estado estado;

    public Sesion() {
    }

    public Sesion(Integer usuarioId, String token, LocalDateTime expiresAt) {
        this.usuarioId = usuarioId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.estado = Estado.ACTIVA;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    /**
     * Estado de la sesión.
     * - ACTIVA: Sesión en vigor y válida.
     * - EXPIRADA: Sesión fuera de tiempo.
     * - REVOCADA: Sesión terminada explícitamente.
     */
    public enum Estado {
        ACTIVA, EXPIRADA, REVOCADA
    }
}
