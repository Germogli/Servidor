package com.germogli.backend.common.security;

import com.germogli.backend.authentication.infrastructure.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * VERSION DEBUG: Interceptor más permisivo para debuggear problemas de cookies.
 * TEMPORAL - Permite conexiones sin token para diagnosticar el problema.
 */
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        log.info("🤝 INICIANDO HANDSHAKE WebSocket desde: {} -> {}",
                request.getRemoteAddress(), request.getURI());

        try {
            // DEBUG: Mostrar todos los headers
            logAllHeaders(request);

            // Extraer token JWT de cookies
            String jwtToken = extractJwtFromCookies(request);

            if (jwtToken != null) {
                log.info("🍪 TOKEN ENCONTRADO en cookies");

                // Validar token y crear autenticación
                Authentication authentication = validateTokenAndCreateAuth(jwtToken);

                if (authentication != null) {
                    // Guardar autenticación en atributos de sesión WebSocket
                    attributes.put("SPRING_SECURITY_CONTEXT", authentication);
                    attributes.put("JWT_TOKEN", jwtToken);
                    attributes.put("USERNAME", authentication.getName());
                    attributes.put("AUTHENTICATED", true);

                    log.info("✅ HANDSHAKE EXITOSO para usuario: {}", authentication.getName());
                    return true;
                } else {
                    log.warn("❌ Token JWT INVÁLIDO");
                    // ⚠️ TEMPORAL: Permitir conexión sin autenticación para debug
                    attributes.put("AUTHENTICATED", false);
                    attributes.put("DEBUG_REASON", "Token inválido");
                    log.warn("🔓 PERMITIENDO conexión SIN autenticación (MODO DEBUG)");
                    return true; // ✅ CAMBIO: Permitir conexión
                }
            } else {
                log.warn("⚠️ NO se encontró token JWT en cookies");
                // ⚠️ TEMPORAL: Permitir conexión sin autenticación para debug
                attributes.put("AUTHENTICATED", false);
                attributes.put("DEBUG_REASON", "Sin token JWT");
                log.warn("🔓 PERMITIENDO conexión SIN autenticación (MODO DEBUG)");
                return true; // ✅ CAMBIO: Permitir conexión
            }

        } catch (Exception e) {
            log.error("❌ ERROR durante handshake WebSocket: {}", e.getMessage(), e);
            // ⚠️ TEMPORAL: Permitir conexión incluso con errores
            attributes.put("AUTHENTICATED", false);
            attributes.put("DEBUG_REASON", "Error: " + e.getMessage());
            log.warn("🔓 PERMITIENDO conexión CON ERROR (MODO DEBUG)");
            return true; // ✅ CAMBIO: Permitir conexión
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

        if (exception != null) {
            log.error("❌ Error después del handshake WebSocket: {}", exception.getMessage());
        } else {
            log.info("✅ Handshake WebSocket COMPLETADO");
        }
    }

    /**
     * DEBUG: Mostrar todos los headers para diagnosticar.
     */
    private void logAllHeaders(ServerHttpRequest request) {
        log.info("🔍 HEADERS del request:");
        request.getHeaders().forEach((name, values) -> {
            log.info("  📋 {}: {}", name, values);
        });

        // Si es ServletServerHttpRequest, mostrar cookies específicamente
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            Cookie[] cookies = servletRequest.getCookies();

            log.info("🍪 COOKIES disponibles:");
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    log.info("  🍪 {}: {} (domain: {}, path: {}, secure: {}, httpOnly: {})",
                            cookie.getName(),
                            cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..." : cookie.getValue(),
                            cookie.getDomain(),
                            cookie.getPath(),
                            cookie.getSecure(),
                            cookie.isHttpOnly());
                }
            } else {
                log.warn("  ❌ NO HAY COOKIES en el request");
            }

            // DEBUG: Mostrar info adicional del request
            log.info("🌐 REQUEST INFO:");
            log.info("  📍 Remote Address: {}", servletRequest.getRemoteAddr());
            log.info("  🌍 Remote Host: {}", servletRequest.getRemoteHost());
            log.info("  🔗 Request URL: {}", servletRequest.getRequestURL());
            log.info("  📡 Origin: {}", servletRequest.getHeader("Origin"));
            log.info("  🆔 User-Agent: {}", servletRequest.getHeader("User-Agent"));
        }
    }

    /**
     * Extrae el token JWT de las cookies del request HTTP.
     */
    private String extractJwtFromCookies(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            Cookie[] cookies = servletRequest.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (JwtService.JWT_COOKIE_NAME.equals(cookie.getName())) {
                        String token = cookie.getValue();
                        log.info("🎯 TOKEN JWT encontrado: {}...",
                                token.substring(0, Math.min(20, token.length())));
                        return token;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Valida el token JWT y crea un objeto Authentication.
     */
    private Authentication validateTokenAndCreateAuth(String token) {
        try {
            String username = jwtService.getUsernameFromToken(token);

            if (username != null) {
                log.info("👤 Usuario extraído del token: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    log.info("✅ Token JWT VÁLIDO para usuario: {}", username);

                    return new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                } else {
                    log.warn("❌ Token JWT EXPIRADO para usuario: {}", username);
                }
            } else {
                log.warn("❌ No se pudo extraer USERNAME del token JWT");
            }

        } catch (Exception e) {
            log.error("❌ ERROR validando token JWT: {}", e.getMessage());
        }

        return null;
    }
}