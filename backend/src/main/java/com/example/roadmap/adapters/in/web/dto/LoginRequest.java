package com.example.roadmap.adapters.in.web.dto;

/**
 * DTO para solicitud de login.
 *
 * Contiene el username y password en formato plano.
 * El backend encripta la contraseña con BCrypt para validación.
 *
 * @since 1.0
 */
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
