package com.germogli.backend.community.message.web.controller;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.MessageDeliveryException;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.message.application.dto.CreateMessageRequestDTO;
import com.germogli.backend.community.message.application.dto.MessageWebSocketDTO;
import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.service.MessageDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Controlador para gestionar mensajes en tiempo real a través de WebSockets.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketController {

    private final MessageDomainService messageDomainService;
    private final CommunitySharedService sharedService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Verifica que el usuario esté autenticado correctamente.
     *
     * @param headerAccessor Acceso a headers STOMP para información adicional
     * @return La autenticación verificada
     * @throws AccessDeniedException si el usuario no está autenticado
     */
    private Authentication checkAuthentication(SimpMessageHeaderAccessor headerAccessor) {
        // Intentar obtener autenticación del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si no está en el contexto, intentar obtenerla del mensaje
        if ((auth == null || !auth.isAuthenticated()) && headerAccessor != null && headerAccessor.getUser() != null) {
            auth = (Authentication) headerAccessor.getUser();
            // Restaurar en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Verificación final
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            log.error("Usuario no autenticado intentando enviar mensaje");
            throw new AccessDeniedException("Usuario no autenticado");
        }

        return auth;
    }

    /**
     * Maneja mensajes enviados a grupos específicos.
     */
    @MessageMapping("/message/group/{groupId}")
    public void handleGroupMessage(
            @DestinationVariable Integer groupId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Recibido mensaje para grupo: {}", groupId);

        try {
            // Verificación de autenticación y obtención del objeto Authentication
            Authentication auth = checkAuthentication(headerAccessor);
            log.debug("Usuario autenticado correctamente: {}", auth.getName());

            // Obtener usuario completo del dominio usando el servicio compartido
            UserDomain currentUser = sharedService.getAuthenticatedUser();

            // Enriquecer mensaje con datos del usuario
            enrichMessageWithUserData(message, currentUser);
            message.setGroupId(groupId);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "group");
            log.debug("Mensaje persistido con ID: {}", savedMessage.getId());

            // Preparar mensaje para envío (añadir ID y timestamp)
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar mensaje al tópico del grupo
            messagingTemplate.convertAndSend("/topic/message/group/" + groupId, message);
            log.debug("Mensaje enviado correctamente al grupo {}", groupId);

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para grupo " + groupId);
        }
    }

    // Otros métodos para thread, post, etc.

    /**
     * Maneja errores durante el procesamiento de mensajes.
     */
    private void handleMessageError(Exception e, SimpMessageHeaderAccessor headerAccessor, String context) {
        log.error("{}: {}", context, e.getMessage(), e);

        // Notificar al usuario del error si es posible determinar su session
        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/queue/errors",
                    new ErrorResponse("Acceso denegado: No tiene permisos para esta operación",
                            e instanceof AccessDeniedException ? "FORBIDDEN" : "SERVER_ERROR")
            );
        }

        // Propagar excepción para que sea manejada por el controlador global
        if (e instanceof AccessDeniedException) {
            throw (AccessDeniedException) e;
        } else {
            throw new MessageDeliveryException(context + ": " + e.getMessage(), e);
        }
    }

    /**
     * Clase de respuesta de error para el cliente
     */
    private static class ErrorResponse {
        private final String message;
        private final String type;
        private final long timestamp;

        public ErrorResponse(String message, String type) {
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Enriquece el mensaje con datos del usuario autenticado.
     */
    private void enrichMessageWithUserData(MessageWebSocketDTO message, UserDomain currentUser) {
        message.setUserId(currentUser.getId());
        message.setUsername(currentUser.getUsername());
        message.setUserAvatar(currentUser.getAvatar());
        message.setTimestamp(LocalDateTime.now());
    }

    /**
     * Persiste el mensaje en la base de datos.
     */
    private MessageDomain persistMessage(MessageWebSocketDTO message, String contextType) {
        CreateMessageRequestDTO createRequest = CreateMessageRequestDTO.builder()
                .postId(message.getPostId())
                .content(message.getContent())
                .threadId(message.getThreadId())
                .groupId(message.getGroupId())
                .build();

        return messageDomainService.createMessage(createRequest);
    }

    /**
     * Manejador global de excepciones para mensajes.
     */
    @MessageExceptionHandler
    public void handleException(Exception exception, Principal principal) {
        log.error("Error en mensaje WebSocket: {}", exception.getMessage(), exception);

        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new ErrorResponse(
                            "Error: " + exception.getMessage(),
                            exception instanceof AccessDeniedException ? "AUTH_ERROR" : "SERVER_ERROR"
                    )
            );
        }
    }
}