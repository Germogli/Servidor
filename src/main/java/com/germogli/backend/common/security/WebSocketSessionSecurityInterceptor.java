package com.germogli.backend.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Interceptor de canal WebSocket simplificado que utiliza la autenticación
 * almacenada durante el handshake para establecer el contexto de seguridad.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WebSocketSessionSecurityInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        SimpMessageType messageType = accessor.getMessageType();

        try {
            // Para mensajes de CONEXIÓN
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
            log.error("Error en interceptor de sesión WebSocket: {}", e.getMessage(), e);
        }

        return message;
    }

    /**
     * Maneja mensajes de conexión recuperando la autenticación de los atributos de sesión.
     */
    private void handleConnectMessage(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            Authentication auth = (Authentication) sessionAttributes.get("SPRING_SECURITY_CONTEXT");

            if (auth != null) {
                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);

                String username = (String) sessionAttributes.get("USERNAME");
                log.debug("✅ Autenticación establecida desde sesión para usuario: {}", username);
            } else {
                log.warn("❌ No se encontró autenticación en atributos de sesión");
            }
        } else {
            log.warn("❌ No hay atributos de sesión disponibles");
        }
    }

    /**
     * Maneja mensajes regulares propagando la autenticación.
     */
    private void handleSendMessage(SimpMessageHeaderAccessor accessor) {
        Authentication auth = null;

        // Intentar obtener autenticación del usuario en el accessor
        if (accessor.getUser() instanceof Authentication) {
            auth = (Authentication) accessor.getUser();
        }

        // Si no está en el accessor, intentar recuperar de atributos de sesión
        if (auth == null) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                auth = (Authentication) sessionAttributes.get("SPRING_SECURITY_CONTEXT");
            }
        }

        if (auth != null) {
            // Establecer contexto de seguridad
            SecurityContext securityContext = new SecurityContextImpl();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);
            accessor.setUser(auth);

            log.trace("🔄 Contexto de seguridad actualizado para: {}", auth.getName());
        } else {
            log.warn("⚠️ No se pudo recuperar autenticación para mensaje");
        }
    }

    /**
     * Propaga la autenticación al contexto de seguridad.
     */
    private void propagateAuthentication(SimpMessageHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication) {
            Authentication auth = (Authentication) accessor.getUser();
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        // Limpiar contexto de seguridad
        SecurityContextHolder.clearContext();
    }
}