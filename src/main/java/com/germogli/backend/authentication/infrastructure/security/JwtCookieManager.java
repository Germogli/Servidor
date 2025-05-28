package com.germogli.backend.authentication.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utilidad para gestionar cookies JWT.
 * Proporciona métodos para crear, añadir y extraer cookies de forma segura.
 */
@Component
public class JwtCookieManager {

    /**
     * Añade una cookie segura con el token JWT a la respuesta HTTP.
     * La cookie se configura como HttpOnly, Secure, y con SameSite=Strict.
     *
     * @param response Respuesta HTTP donde se añadirá la cookie
     * @param token Token JWT a incluir en la cookie
     */
    public void addJwtCookie(HttpServletResponse response, String token) {
        // Crear cookie usando ResponseCookie builder (compatible con atributos modernos)
        ResponseCookie cookie = ResponseCookie.from(JwtService.JWT_COOKIE_NAME, token)
                .httpOnly(true)               // Previene acceso JavaScript (protección XSS)
                .secure(false)                 //// HTTP local permitido
                .path("/")                    // Disponible en toda la aplicación
                .maxAge(JwtService.JWT_COOKIE_EXPIRY_SECONDS)  // Duración de la cookie
                .sameSite("None")           //Menos restrictivo que Strict
                .build();

        // Añadir cookie al header de la respuesta
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        //HEADER ADICIONAL para mejor compatibilidad cross-origin
        response.addHeader("Access-Control-Expose-Headers", "Set-Cookie");
    }

    /**
     * Invalida la cookie JWT en la respuesta HTTP.
     * Establece un valor vacío y expiración inmediata.
     *
     * @param response Respuesta HTTP donde se invalidará la cookie
     */
    public void clearJwtCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(JwtService.JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)                   // Expiración inmediata
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Extrae el token JWT de las cookies en la solicitud HTTP.
     *
     * @param request Solicitud HTTP de la que se extraerán las cookies
     * @return El valor del token JWT o null si no está presente
     */
    public String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtService.JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}