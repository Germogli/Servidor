package com.germogli.backend.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor especial para WebSockets con máxima prioridad.
 * Garantiza que el SecurityContext esté siempre disponible para mensajes STOMP.
 * <p>
 * Esta implementación utiliza un enfoque directo para establecer la autenticación
 * en el SecurityContext del hilo actual, justo antes de que el controlador de mensajes
 * invoque el método anotado con @PreAuthorize.
 *
 * @author [Tu nombre]
 * @version 3.0
 * @since 2025-05-04
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSecurityInterceptor.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message);

        // Solo procesamos mensajes STOMP de tipo CONNECT, SUBSCRIBE, SEND
        if (accessor != null && accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            logger.debug("Procesando mensaje de usuario: {}", username);

            // Buscar el UserDetails y crear una autenticación válida
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Establecer la autenticación en el SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Autenticación establecida en SecurityContext para: {}", username);
            } catch (Exception e) {
                logger.error("Error al establecer autenticación en SecurityContext: {}", e.getMessage());
            }
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}