package com.germogli.backend.common.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor de canal con prioridad MÁXIMA para gestionar el SecurityContext en mensajes WebSocket.
 * <p>
 * Esta implementación corregida garantiza que la autenticación esté disponible
 * en el SecurityContext durante el procesamiento de los mensajes,
 * independientemente del hilo de ejecución.
 *
 * @author [Tu nombre]
 * @version 2.0
 * @since 2025-05-04
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityContextChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Para todos los mensajes, intentamos recuperar la autenticación del User
            if (accessor.getUser() instanceof Authentication) {
                Authentication auth = (Authentication) accessor.getUser();
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Restaurada autenticación: {}", auth.getName());
            }
        }

        return message;
    }

    /**
     * Limpia el SecurityContext después de completar el envío del mensaje.
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // Limpiamos el contexto después de procesar el mensaje para evitar filtraciones
        SecurityContextHolder.clearContext();
        logger.trace("SecurityContext limpiado después de procesamiento de mensaje");
    }
}