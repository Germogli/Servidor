package com.germogli.backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;

/**
 * Controlador de asesoramiento para manejar excepciones que ocurren durante
 * el procesamiento de mensajes WebSocket/STOMP.
 * <p>
 * Este componente complementa el GlobalException existente, manejando específicamente
 * las excepciones que ocurren durante la comunicación WebSocket, ya que estos errores
 * requieren un tratamiento especial para notificar al cliente vía canal de mensajería.
 *
 * @author [Tu nombre]
 * @version 1.0
 * @since 2025-05-04
 */
@ControllerAdvice
public class WebSocketExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Clase para representar mensajes de error enviados al cliente
     */
    public static class WebSocketErrorMessage {
        private final String message;
        private final String type;
        private final long timestamp;

        public WebSocketErrorMessage(String message, String type) {
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Maneja excepciones de autenticación durante procesamiento de mensajes WebSocket.
     * Envía una notificación de error al cliente cuando una operación falla por problemas de autenticación.
     *
     * @param exception Excepción capturada
     * @param principal Principal de seguridad del cliente (puede ser null)
     */
    @MessageExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public void handleAuthenticationException(AuthenticationCredentialsNotFoundException exception, Principal principal) {
        logger.error("Error de autenticación en WebSocket: {}", exception.getMessage());

        // Si tenemos información del usuario, le notificamos el error
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new WebSocketErrorMessage("Error de autenticación: La sesión podría haber expirado", "AUTH_ERROR")
            );
        }
    }

    /**
     * Maneja excepciones de autorización durante procesamiento de mensajes WebSocket.
     * Envía una notificación de error al cliente cuando no tiene permisos para una operación.
     *
     * @param exception Excepción capturada
     * @param principal Principal de seguridad del cliente (puede ser null)
     */
    @MessageExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException exception, Principal principal) {
        logger.error("Error de autorización en WebSocket: {}", exception.getMessage());

        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new WebSocketErrorMessage("Acceso denegado: No tiene permisos para esta operación", "FORBIDDEN")
            );
        }
    }

    /**
     * Maneja excepciones generales durante procesamiento de mensajes WebSocket.
     * Proporciona un mecanismo de último recurso para notificar errores al cliente.
     *
     * @param exception Excepción capturada
     * @param principal Principal de seguridad del cliente (puede ser null)
     */
    @MessageExceptionHandler(Exception.class)
    public void handleException(Exception exception, Principal principal) {
        logger.error("Error procesando mensaje WebSocket: {}", exception.getMessage(), exception);

        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new WebSocketErrorMessage("Error en el servidor: " + exception.getMessage(), "SERVER_ERROR")
            );
        }
    }
}