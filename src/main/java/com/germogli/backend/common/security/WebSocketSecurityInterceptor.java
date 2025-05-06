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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Interceptor de seguridad para WebSockets que garantiza la propagación
 * correcta del contexto de autenticación.
 *
 * @author Germogli Development Team
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Registra el tipo de mensaje para depuración
        SimpMessageType messageType = accessor.getMessageType();
        if (log.isDebugEnabled()) {
            log.debug("Procesando mensaje tipo {}, sessionId={}",
                    messageType, accessor.getSessionId());
        }

        try {
            // Para mensajes de tipo CONNECT, extraer token JWT y autenticar
            if (SimpMessageType.CONNECT.equals(messageType)) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String username = jwtService.getUsernameFromToken(token);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            // Establecer en el mensaje y en el contexto actual
                            accessor.setUser(auth);
                            SecurityContextHolder.getContext().setAuthentication(auth);

                            log.debug("Usuario {} autenticado para WebSocket", username);
                        }
                    }
                }
            }
            // Para CUALQUIER tipo de mensaje (incluyendo SEND), propagar autenticación
            else if (accessor.getUser() != null) {
                // Recuperar autenticación del mensaje
                Authentication auth = (Authentication) accessor.getUser();

                // Restaurar en el contexto de seguridad actual
                SecurityContextHolder.getContext().setAuthentication(auth);

                if (log.isTraceEnabled()) {
                    log.trace("Autenticación restaurada para usuario: {}",
                            auth.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error procesando autenticación WebSocket: {}", e.getMessage(), e);
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        // Limpiar contexto después para evitar fugas de memoria
        SecurityContextHolder.clearContext();
    }
}