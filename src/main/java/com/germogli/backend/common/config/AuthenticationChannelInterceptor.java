package com.germogli.backend.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.security.Principal;

/**
 * Interceptor que garantiza la propagación de la autenticación del usuario
 * desde la sesión WebSocket al SecurityContext durante el procesamiento de mensajes.
 * <p>
 * Este componente resuelve el problema de autenticación en métodos WebSocket anotados
 * con @PreAuthorize("isAuthenticated()") asegurando que el SecurityContext
 * contenga la información de autenticación correcta en el hilo que procesa cada mensaje.
 *
 * @author [Tu nombre]
 * @version 1.0
 * @since 2025-05-04
 */
@Component
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationChannelInterceptor.class);

    /**
     * Método ejecutado antes de enviar un mensaje a través del canal.
     * Propaga la autenticación del usuario desde los atributos de la sesión al SecurityContext.
     *
     * @param message Mensaje STOMP que se está procesando
     * @param channel Canal de mensajería a través del cual se envía el mensaje
     * @return El mensaje original o su versión modificada (en este caso no se modifica)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Obtener el principal de la sesión WebSocket
            Principal principal = accessor.getUser();

            if (principal != null) {
                // Obtener la autenticación existente y establecerla en el SecurityContext
                org.springframework.security.core.Authentication authentication =
                        (org.springframework.security.core.Authentication) principal;

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Autenticación propagada al SecurityContext para: {}",
                        principal.getName());
            } else {
                logger.debug("No se encontró información de autenticación en el mensaje WebSocket");
            }
        }

        return message;
    }

    /**
     * Método ejecutado después de completar el envío del mensaje.
     * Limpia el SecurityContext para evitar filtración de información entre hilos.
     *
     * @param message Mensaje procesado
     * @param channel Canal de mensajería utilizado
     * @param sent Indicador si el mensaje fue enviado correctamente
     * @param ex Excepción lanzada durante el envío, si ocurrió alguna
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // Limpiar el SecurityContext después de procesar el mensaje
        SecurityContextHolder.clearContext();
        logger.trace("SecurityContext limpiado después de procesamiento de mensaje");
    }
}