package com.germogli.backend.common.security;

import com.germogli.backend.authentication.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


/**
 * Interceptor especial para WebSockets con máxima prioridad.
 * Garantiza que el SecurityContext esté siempre disponible para mensajes STOMP.
 * <p>
 * Esta implementación utiliza un enfoque directo para establecer la autenticación
 * en el SecurityContext del hilo actual, justo antes de que el controlador de mensajes
 * invoque el método anotado con @PreAuthorize.
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getMessageType() == SimpMessageType.CONNECT) {
            // Extraer token JWT del header de conexión
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Validar token y obtener nombre de usuario
                    String username = jwtService.getUsernameFromToken(token);
                    if (username != null) {
                        // Obtener detalles de usuario
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        // Verificar token válido
                        if (jwtService.isTokenValid(token, userDetails)) {
                            // Crear autenticación
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            // Importante: Establecer la autenticación en el mensaje
                            accessor.setUser(auth);

                            // También establecer en SecurityContext actual para otros interceptores
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } catch (Exception e) {
                    // Registrar error pero no interrumpir flujo
                    // El error se manejará después en el controlador
                }
            }
        } else if (accessor != null && accessor.getUser() instanceof Authentication) {
            // Para TODOS los mensajes no-CONNECT, restaurar la autenticación del usuario
            // del mensaje al SecurityContext del hilo actual
            Authentication auth = (Authentication) accessor.getUser();
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // Limpiar contexto después de procesar mensaje
        SecurityContextHolder.clearContext();
    }
}