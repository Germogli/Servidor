package com.germogli.backend.common.config;

import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor que garantiza la propagación de la autenticación del usuario
 * desde la sesión WebSocket al SecurityContext durante el procesamiento de mensajes.
 * <p>
 * Esta implementación actualizada colabora con SecurityContextChannelInterceptor
 * para asegurar la presencia de la autenticación en todos los hilos.
 *
 * @author [Tu nombre]
 * @version 2.0
 * @since 2025-05-04
 */
@Component
@Order(10) // Prioridad menor que SecurityContextChannelInterceptor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Solo para mensajes de tipo CONNECT, establecemos la autenticación
            if (accessor.getMessageType() == SimpMessageType.CONNECT) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    // Establecer User es crucial - es la forma en que Spring STOMP mantiene la autenticación
                    accessor.setUser(auth);
                    logger.debug("Autenticación propagada al SecurityContext para: {}", auth.getName());
                }
            }
        }

        return message;
    }
}