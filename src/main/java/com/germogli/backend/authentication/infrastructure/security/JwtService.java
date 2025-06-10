package com.germogli.backend.authentication.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para la generación y validación de tokens JWT.
 * Soporta autenticación basada en cookies seguras.
 */
@Service
public class JwtService {
    // Clave secreta para firmar el token (se debe mantener segura y con al menos 256 bits).
    private String SECRET_KEY = System.getenv("JWT_SECRET");

    // Nombre de la cookie para el token JWT
    public static String JWT_COOKIE_NAME = System.getenv("COOKIE_NAME");

    // Tiempo de expiración del token en milisegundos (1 dia)
    private static final long JWT_EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    // Tiempo de expiración de la cookie en segundos (1 dia)
    public static final int JWT_COOKIE_EXPIRY_SECONDS = 60 * 60 * 24;

    /**
     * Genera un token JWT para el usuario proporcionado.
     *
     * @param user UserDetails del usuario.
     * @return Token JWT.
     */
    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    /**
     * Genera un token JWT incluyendo el rol del usuario.
     *
     * @param user UserDetails del usuario.
     * @param roleType Tipo de rol del usuario.
     * @return Token JWT con información de rol.
     */
    public String getTokenWithRole(UserDetails user, String roleType) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", roleType);
        return getToken(extraClaims, user);
    }

    /**
     * Genera un token JWT con claims adicionales.
     *
     * @param extraClaims Claims adicionales.
     * @param user        UserDetails del usuario.
     * @return Token JWT.
     */
    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Decodifica la clave secreta para la firma del token.
     *
     * @return Objeto Key.
     */
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el username del token JWT.
     *
     * @param token Token JWT.
     * @return Username.
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el rol del token JWT.
     *
     * @param token Token JWT.
     * @return Rol del usuario o null si no existe.
     */
    public String getRoleFromToken(String token) {
        return getClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Verifica la validez del token para el usuario dado.
     *
     * @param token       Token JWT.
     * @param userDetails UserDetails del usuario.
     * @return true si el token es válido.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Obtiene todos los claims del token.
     *
     * @param token Token JWT.
     * @return Claims.
     */
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtiene un claim específico del token.
     *
     * @param token           Token JWT.
     * @param claimsResolver  Función para extraer el claim.
     * @param <T>             Tipo del claim.
     * @return Valor del claim.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene la fecha de expiración del token.
     *
     * @param token Token JWT.
     * @return Fecha de expiración.
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT.
     * @return true si el token ha expirado.
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}