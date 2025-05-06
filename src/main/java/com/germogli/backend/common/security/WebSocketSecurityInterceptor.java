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

/**
 * Interceptor de seguridad para WebSockets que garantiza la propagación
 * correcta del contexto de autenticación.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j

public class WebSocketSecurityInterceptor implements ChannelInterceptor {
    static {
        // Habilitar logging detallado para diagnóstico
        System.setProperty("org.springframework.web.socket.DEBUG", "true");
    }

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
        // Log detallado del mensaje entrante
        log.debug("STOMP Mensaje entrante - Tipo: {}, Destino: {}, ID Sesión: {}",
                accessor.getMessageType(),
                accessor.getDestination(),
                accessor.getSessionId());
        // Log de todos los headers para diagnóstico
        accessor.toNativeHeaderMap().forEach((key, value) -> {
            log.debug("Header STOMP: {} = {}", key, value);
        });

        // Obtener tipo de mensaje para procesamiento específico
        SimpMessageType messageType = accessor.getMessageType();

        if (log.isDebugEnabled()) {
            log.debug("Procesando mensaje tipo {}, sessionId={}, destino={}",
                    messageType, accessor.getSessionId(), accessor.getDestination());
        }

        try {
            // 1. Para mensajes de CONEXIÓN inicial
            if (SimpMessageType.CONNECT.equals(messageType)) {
                handleConnectMessage(accessor);
            }
            // 2. Para mensajes ENVIADOS por el cliente (crítico)
            else if (SimpMessageType.MESSAGE.equals(messageType)) {
                handleSendMessage(accessor);
            }
            // 3. Para otros tipos de mensajes (SUBSCRIBE, etc.)
            else if (accessor.getUser() != null) {
                propagateAuthentication(accessor);
            }
        } catch (Exception e) {
            log.error("Error procesando autenticación en mensaje WebSocket: {}", e.getMessage(), e);
        }

        return message;
    }

    /**
     * Maneja la autenticación en mensajes de tipo CONNECT (conexión inicial).
     */
    private void handleConnectMessage(SimpMessageHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        Authentication auth = extractAndValidateToken(authHeader);

        if (auth != null) {
            // Establecer autenticación en el mensaje y en contexto de seguridad
            accessor.setUser(auth);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Autenticación establecida para conexión WebSocket: usuario={}",
                    auth.getName());
        } else {
            log.warn("Intento de conexión WebSocket sin token JWT válido");
        }
    }

    /**
     * Maneja la autenticación en mensajes de tipo MESSAGE (envío de mensajes).
     */
    private void handleSendMessage(SimpMessageHeaderAccessor accessor) {
        // 1. Intentar obtener autenticación del mensaje
        Authentication auth = null;

        if (accessor.getUser() != null && accessor.getUser() instanceof Authentication) {
            auth = (Authentication) accessor.getUser();
            log.debug("Autenticación recuperada del mensaje: {}", auth.getName());
        }
        // 2. Si no está en el mensaje, intentar extraer de cabeceras
        else {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            auth = extractAndValidateToken(authHeader);

            if (auth != null) {
                log.debug("Autenticación recuperada de cabecera en mensaje MESSAGE: {}",
                        auth.getName());
            }
        }

        // 3. Si tenemos autenticación, establecerla en contexto actual y mensaje
        if (auth != null) {
            // Crear contexto de seguridad nuevo para este hilo
            SecurityContext securityContext = new SecurityContextImpl();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);

            // También establecer en el mensaje para propagación
            accessor.setUser(auth);

            log.debug("Contexto de seguridad establecido para mensaje MESSAGE: {}",
                    auth.getName());
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

        if (log.isTraceEnabled()) {
            log.trace("Autenticación propagada: {}", auth.getName());
        }
    }

    /**
     * Extrae y valida el token JWT de la cabecera de autorización.
     */
    private Authentication extractAndValidateToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.getUsernameFromToken(token);

            if (username != null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails)) {
                        return new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                    } else {
                        log.warn("Token JWT inválido para usuario: {}", username);
                    }
                } catch (Exception e) {
                    log.error("Error cargando detalles de usuario desde token: {}", e.getMessage());
                }
            }
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