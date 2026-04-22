package com.example.roadmap.domain;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Usuario del sistema.
 *
 * Representa un usuario con credenciales, rol y estado.
 * Auditoría incluida mediante created_by/updated_by.
 *
 * @since 1.0
 */
public class Usuario {
    private Integer id;
    private String username;
    private String passwordHash;
    private Role rol;
    private Boolean activo;
    private Boolean mustChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public Usuario() {
    }

    public Usuario(String username, String passwordHash, Role rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol != null ? rol : Role.CONSULTA;
        this.activo = true;
        this.mustChangePassword = true;
    }

    // Getters y Setters
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Rol disponible en el sistema.
     * - ADMIN: Acceso total, gestión de usuarios y roles.
     * - GESTION: Lectura + creación y edición de roadmaps y sus componentes.
     * - CONSULTA: Solo lectura de datos.
     */
    public enum Role {
        ADMIN, GESTION, CONSULTA
    }
}
