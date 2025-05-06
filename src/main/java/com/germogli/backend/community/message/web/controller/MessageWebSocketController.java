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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

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
     * Implementa verificación explícita del SecurityContext.
     *
     * @param headerAccessor Acceso a headers STOMP para información adicional
     * @return La autenticación verificada
     * @throws AccessDeniedException si el usuario no está autenticado
     */
    private Authentication checkAuthentication(SimpMessageHeaderAccessor headerAccessor) {
        // Obtener autenticación directamente del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Loguear estado actual del contexto de seguridad
        log.debug("Estado de SecurityContext al procesar mensaje: auth={}, principal={}",
                auth != null,
                auth != null ? auth.getPrincipal() : "null");

        // Si no está en el contexto, intentar recuperarla del mensaje
        if ((auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null)
                && headerAccessor != null && headerAccessor.getUser() != null) {

            log.debug("Autenticación no encontrada en SecurityContext, recuperando del mensaje");
            auth = (Authentication) headerAccessor.getUser();

            // Restaurar en el contexto de seguridad si se encuentra en mensaje
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Autenticación restaurada desde mensaje: {}", auth.getName());
        }

        // Verificación final - lanzar excepción si no está autenticado
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            log.error("⚠️ Usuario no autenticado intentando enviar mensaje");
            throw new AuthenticationCredentialsNotFoundException(
                    "No se encontró autenticación en el contexto de seguridad");
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
            // Verificación explícita de autenticación
            Authentication auth = checkAuthentication(headerAccessor);
            log.debug("Usuario autenticado correctamente: {}", auth.getName());

            // Obtener usuario completo del dominio usando el servicio compartido
            UserDomain currentUser = sharedService.getAuthenticatedUser();
            log.debug("Usuario de dominio recuperado: ID={}, username={}",
                    currentUser.getId(), currentUser.getUsername());

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

    /**
     * Maneja mensajes enviados a hilos específicos.
     */
    @MessageMapping("/message/thread/{threadId}")
    public void handleThreadMessage(
            @DestinationVariable Integer threadId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Recibido mensaje para hilo: {}", threadId);

        try {
            // Verificación explícita de autenticación
            Authentication auth = checkAuthentication(headerAccessor);
            log.debug("Usuario autenticado correctamente: {}", auth.getName());

            // Obtener usuario completo del dominio
            UserDomain currentUser = sharedService.getAuthenticatedUser();

            // Enriquecer mensaje con datos del usuario
            enrichMessageWithUserData(message, currentUser);
            message.setThreadId(threadId);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "thread");

            // Preparar mensaje para envío
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar mensaje al tópico del hilo
            messagingTemplate.convertAndSend("/topic/message/thread/" + threadId, message);
            log.debug("Mensaje enviado correctamente al hilo {}", threadId);

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para hilo " + threadId);
        }
    }

    /**
     * Maneja mensajes enviados a publicaciones específicas.
     */
    @MessageMapping("/message/post/{postId}")
    public void handlePostMessage(
            @DestinationVariable Integer postId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Recibido mensaje para publicación: {}", postId);

        try {
            // Verificación explícita de autenticación
            Authentication auth = checkAuthentication(headerAccessor);
            log.debug("Usuario autenticado correctamente: {}", auth.getName());

            // Obtener usuario completo del dominio
            UserDomain currentUser = sharedService.getAuthenticatedUser();

            // Enriquecer mensaje con datos del usuario
            enrichMessageWithUserData(message, currentUser);
            message.setPostId(postId);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "post");

            // Preparar mensaje para envío
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar mensaje al tópico de la publicación
            messagingTemplate.convertAndSend("/topic/message/post/" + postId, message);
            log.debug("Mensaje enviado correctamente a la publicación {}", postId);

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para publicación " + postId);
        }
    }

    /**
     * Maneja mensajes enviados al foro general.
     */
    @MessageMapping("/message/forum")
    public void handleForumMessage(
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Recibido mensaje para foro general");

        try {
            // Verificación explícita de autenticación
            Authentication auth = checkAuthentication(headerAccessor);
            log.debug("Usuario autenticado correctamente: {}", auth.getName());

            // Obtener usuario completo del dominio
            UserDomain currentUser = sharedService.getAuthenticatedUser();

            // Enriquecer mensaje con datos del usuario
            enrichMessageWithUserData(message, currentUser);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "forum");

            // Preparar mensaje para envío
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar mensaje al tópico del foro
            messagingTemplate.convertAndSend("/topic/message/forum", message);
            log.debug("Mensaje enviado correctamente al foro general");

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para foro general");
        }
    }

    /**
     * Maneja errores durante el procesamiento de mensajes.
     */
    private void handleMessageError(Exception e, SimpMessageHeaderAccessor headerAccessor, String context) {
        log.error("{}: {}", context, e.getMessage(), e);

        // Notificar al usuario del error
        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/queue/errors",
                    new ErrorResponse(
                            e instanceof AccessDeniedException || e instanceof AuthenticationCredentialsNotFoundException
                                    ? "Error de autenticación: " + e.getMessage()
                                    : "Error en el servidor: " + e.getMessage(),
                            e instanceof AccessDeniedException ? "AUTH_ERROR" : "SERVER_ERROR"
                    )
            );
        }

        // Propagar excepción para ser manejada por el controlador global
        if (e instanceof AccessDeniedException) {
            throw (AccessDeniedException) e;
        } else if (e instanceof AuthenticationCredentialsNotFoundException) {
            throw (AuthenticationCredentialsNotFoundException) e;
        } else {
            throw new MessageDeliveryException(context + ": " + e.getMessage(), e);
        }
    }

    /**
     * Manejador global de excepciones para mensajes WebSocket.
     */
    @MessageExceptionHandler
    public void handleException(Exception exception, SimpMessageHeaderAccessor headerAccessor) {
        log.error("Error global en mensaje WebSocket: {}", exception.getMessage(), exception);

        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/queue/errors",
                    new ErrorResponse(
                            "Error en procesamiento de mensaje: " + exception.getMessage(),
                            "ERROR"
                    )
            );
        }
    }

    /**
     * Respuesta de error para el cliente.
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
}