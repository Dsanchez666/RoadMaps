package com.example.roadmap.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Servicio para generar, validar y extraer información de tokens JWT.
 *
 * Se configura mediante propiedades de Spring.
 * Usa HS256 para la firma.
 *
 * @since 1.0
 */
@Service
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-change-in-production-must-be-at-least-256-bits}")
    private String secretKey;

    @Value("${jwt.expiration:28800000}") // 8 horas por defecto
    private long expirationTime;

    /**
     * Genera un token JWT para un usuario.
     *
     * @param usuarioId ID del usuario.
     * @param username Username del usuario.
     * @param rol Rol del usuario.
     * @return Token JWT.
     */
    private Key signingKey;

    private Key getSigningKey() {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
        return signingKey;
    }

    public String generateToken(Integer usuarioId, String username, String rol) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .setSubject(username)
            .claim("usuarioId", usuarioId)
            .claim("rol", rol)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Valida un token JWT.
     *
     * @param token Token a validar.
     * @return true si el token es válido, false si está expirado o es inválido.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el username del token.
     *
     * @param token Token JWT.
     * @return Username si el token es válido.
     * @throws JwtException Si el token es inválido.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    /**
     * Extrae el ID de usuario del token.
     *
     * @param token Token JWT.
     * @return ID de usuario si el token es válido.
     * @throws JwtException Si el token es inválido.
     */
    public Integer getUsuarioIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        Object value = claims.get("usuarioId");
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }
        return null;
    }

    /**
     * Extrae el rol del token.
     *
     * @param token Token JWT.
     * @return Rol si el token es válido.
     * @throws JwtException Si el token es inválido.
     */
    public String getRolFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claims.get("rol", String.class);
    }

    /**
     * Retorna el tiempo de expiración configurado en ms.
     *
     * @return Tiempo de expiración.
     */
    public long getExpirationTime() {
        return expirationTime;
    }
}
