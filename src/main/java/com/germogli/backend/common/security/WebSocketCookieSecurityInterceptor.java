package com.germogli.backend.common.security;

import com.germogli.backend.authentication.infrastructure.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Interceptor de seguridad para WebSockets que maneja autenticación por cookies JWT.
 * Este interceptor extrae el token JWT de las cookies HTTP y establece la autenticación
 * en el contexto de seguridad para las conexiones WebSocket.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WebSocketCookieSecurityInterceptor implements ChannelInterceptor {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

        if (accessor == null) {
            log.warn("Accessor es null, retornando mensaje sin procesar");
            return message;
        }

        SimpMessageType messageType = accessor.getMessageType();
        log.debug("Procesando mensaje tipo: {}, sessionId: {}", messageType, accessor.getSessionId());

        try {
            // Para mensajes de CONEXIÓN inicial
            if (SimpMessageType.CONNECT.equals(messageType)) {
                handleConnectMessage(accessor);
            }
            // Para mensajes enviados por el cliente
            else if (SimpMessageType.MESSAGE.equals(messageType)) {
                handleSendMessage(accessor);
            }
            // Para otros tipos de mensajes
            else if (accessor.getUser() != null) {
                propagateAuthentication(accessor);
            }
        } catch (Exception e) {
            log.error("Error procesando autenticación en mensaje WebSocket: {}", e.getMessage(), e);
        }

        return message;
    }

    /**
     * Maneja la autenticación en mensajes de tipo CONNECT.
     * Extrae el token JWT de las cookies del request HTTP inicial.
     */
    private void handleConnectMessage(SimpMessageHeaderAccessor accessor) {
        // Intentar extraer token de cookies usando headers de transporte HTTP
        String jwtToken = extractJwtFromWebSocketHeaders(accessor);

        if (jwtToken == null) {
            // Fallback: intentar extraer de headers nativos de STOMP
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                log.debug("Token JWT extraído de header Authorization");
            }
        }

        if (jwtToken != null) {
            Authentication auth = validateAndCreateAuthentication(jwtToken);
            if (auth != null) {
                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Autenticación establecida para conexión WebSocket: usuario={}",
                        auth.getName());
            } else {
                log.warn("Token JWT inválido en conexión WebSocket");
            }
        } else {
            log.warn("No se encontró token JWT en cookies ni headers para conexión WebSocket");
        }
    }

    /**
     * Maneja la autenticación en mensajes de tipo MESSAGE.
     */
    private void handleSendMessage(SimpMessageHeaderAccessor accessor) {
        Authentication auth = null;

        // 1. Intentar obtener autenticación del mensaje
        if (accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
            auth = (Authentication) accessor.getUser();
            log.debug("Autenticación recuperada del mensaje: {}", auth.getName());
        }

        // 2. Si tenemos autenticación, establecerla en contexto actual
        if (auth != null) {
            SecurityContext securityContext = new SecurityContextImpl();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);
            accessor.setUser(auth);
            log.debug("Contexto de seguridad establecido para mensaje MESSAGE: {}", auth.getName());
        } else {
            log.warn("⚠️ No se pudo recuperar autenticación para mensaje MESSAGE");
        }
    }

    /**
     * Propaga la autenticación desde el mensaje al contexto de seguridad.
     */
    private void propagateAuthentication(SimpMessageHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.trace("Autenticación propagada: {}", auth.getName());
    }

    /**
     * Extrae el token JWT de las cookies usando los headers de transporte WebSocket.
     * Este método intenta acceder a las cookies del request HTTP inicial.
     */
    private String extractJwtFromWebSocketHeaders(SimpMessageHeaderAccessor accessor) {
        try {
            // Obtener headers del transporte HTTP subyacente
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                log.debug("Atributos de sesión disponibles: {}", sessionAttributes.keySet());
            }

            // Intentar extraer cookies de headers nativos que puedan contener información de cookies
            Map<String, Object> nativeHeaders = (Map<String, Object>) accessor.getHeader("nativeHeaders");
            if (nativeHeaders != null) {
                Object cookieHeader = nativeHeaders.get("cookie");
                if (cookieHeader != null) {
                    String cookieString = cookieHeader.toString();
                    return extractJwtFromCookieString(cookieString);
                }
            }

            // Fallback: buscar en todos los headers nativos
            accessor.toNativeHeaderMap().forEach((key, values) -> {
                log.debug("Header nativo: {} = {}", key, values);
                if ("cookie".equalsIgnoreCase(key) && values != null && !values.isEmpty()) {
                    String cookieString = values.get(0);
                    log.debug("Cookie string encontrado: {}", cookieString);
                }
            });

        } catch (Exception e) {
            log.error("Error extrayendo JWT de headers WebSocket: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Extrae el token JWT de una cadena de cookies.
     */
    private String extractJwtFromCookieString(String cookieString) {
        if (cookieString == null || cookieString.isEmpty()) {
            return null;
        }

        // Parsear cookies manualmente
        String[] cookies = cookieString.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && JwtService.JWT_COOKIE_NAME.equals(parts[0].trim())) {
                log.debug("Token JWT encontrado en cookie: {}", parts[1].substring(0, Math.min(10, parts[1].length())) + "...");
                return parts[1].trim();
            }
        }

        return null;
    }

    /**
     * Valida el token JWT y crea un objeto de autenticación.
     */
    private Authentication validateAndCreateAuthentication(String token) {
        try {
            String username = jwtService.getUsernameFromToken(token);
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    return new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                } else {
                    log.warn("Token JWT inválido para usuario: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Error validando token JWT: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        // Limpiar contexto después para evitar fugas de memoria
        SecurityContextHolder.clearContext();
    }
}