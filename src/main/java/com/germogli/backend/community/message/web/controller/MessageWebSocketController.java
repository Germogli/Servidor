package com.germogli.backend.community.message.web.controller;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.MessageDeliveryException;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.message.application.dto.CreateMessageRequestDTO;
import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import com.germogli.backend.community.message.application.dto.MessageWebSocketDTO;
import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.service.MessageDomainService;
import com.germogli.backend.community.message.infrastructure.cache.MessageCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Controlador para gestionar mensajes en tiempo real a través de WebSockets.
 * ✅ ARREGLADO: Extrae identidad directamente del mensaje WebSocket para evitar contaminación cruzada
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketController {

    private final MessageDomainService messageDomainService;
    private final CommunitySharedService sharedService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageCache messageCache;

    // ✅ INYECTAR: Repositorio para buscar usuario sin usar SecurityContext
    @Qualifier("AuthenticationUserRepository")
    private final UserDomainRepository userRepository;

    /**
     * ✅ NUEVO: Extrae UserDomain directamente del mensaje WebSocket SIN usar SecurityContext global
     */
    private UserDomain extractUserFromMessage(SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 1. Obtener Authentication directamente del mensaje WebSocket
            Authentication auth = null;

            if (headerAccessor != null && headerAccessor.getUser() instanceof Authentication) {
                auth = (Authentication) headerAccessor.getUser();
                log.debug("🔍 Autenticación extraída del mensaje: {}", auth.getName());
            }

            // 2. Si no está en el mensaje, intentar del contexto como fallback
            if (auth == null) {
                auth = SecurityContextHolder.getContext().getAuthentication();
                log.debug("🔍 Autenticación extraída del contexto como fallback: {}",
                        auth != null ? auth.getName() : "null");
            }

            if (auth == null || !auth.isAuthenticated()) {
                throw new AuthenticationCredentialsNotFoundException("No se encontró autenticación válida");
            }

            // 3. ✅ CRÍTICO: Extraer username directamente de la Authentication
            String username;
            if (auth.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) auth.getPrincipal()).getUsername();
            } else {
                username = auth.getName();
            }

            log.debug("👤 Username extraído: {}", username);

            // 4. ✅ BUSCAR USER DIRECTAMENTE en repositorio SIN usar SecurityContext
            UserDomain user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

            log.debug("✅ Usuario obtenido directamente: ID={}, username={}",
                    user.getId(), user.getUsername());

            return user;

        } catch (Exception e) {
            log.error("❌ Error extrayendo usuario del mensaje: {}", e.getMessage(), e);
            throw new AuthenticationCredentialsNotFoundException("Error obteniendo identidad del usuario", e);
        }
    }

    /**
     * ✅ ACTUALIZADO: Usa identidad extraída directamente del mensaje
     */
    private MessageDomain persistMessage(MessageWebSocketDTO message, String contextType, UserDomain user) {

        // ✅ TEMPORAL: Establecer contexto solo para la persistencia si es necesario
        Authentication tempAuth = null;
        boolean contextWasSet = false;

        try {
            // Verificar si hay contexto establecido
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth == null || !currentAuth.isAuthenticated()) {
                // Crear contexto temporal si es necesario para servicios que lo requieren
                tempAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user.toUserDetails(), null, user.toUserDetails().getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(tempAuth);
                contextWasSet = true;
                log.debug("🔒 Contexto temporal establecido para persistencia: {}", user.getUsername());
            }

            CreateMessageRequestDTO createRequest = CreateMessageRequestDTO.builder()
                    .postId(message.getPostId())
                    .content(message.getContent())
                    .threadId(message.getThreadId())
                    .groupId(message.getGroupId())
                    .build();

            MessageDomain savedMessage = messageDomainService.createMessage(createRequest);

            // ✅ Actualizar caché
            updateCacheAfterPersist(savedMessage, contextType);

            return savedMessage;

        } finally {
            // ✅ LIMPIAR contexto temporal si lo establecimos
            if (contextWasSet) {
                SecurityContextHolder.clearContext();
                log.debug("🧹 Contexto temporal limpiado");
            }
        }
    }

    /**
     * ✅ MANTENER: Actualización de caché
     */
    private void updateCacheAfterPersist(MessageDomain savedMessage, String contextType) {
        try {
            MessageResponseDTO responseDTO = messageDomainService.toResponse(savedMessage);

            Integer contextId = switch (contextType) {
                case "group" -> savedMessage.getGroupId();
                case "thread" -> savedMessage.getThreadId();
                case "post" -> savedMessage.getPostId();
                case "forum" -> null;
                default -> throw new IllegalArgumentException("Tipo de contexto inválido: " + contextType);
            };

            messageCache.addMessage(contextType, contextId, responseDTO);
            log.debug("✅ Caché actualizada: contexto={}, id={}", contextType, contextId);

        } catch (Exception e) {
            log.warn("⚠️ Error actualizando caché (no crítico): {}", e.getMessage());
        }
    }

    /**
     * ✅ ARREGLADO: Usa identidad extraída directamente del mensaje
     */
    @MessageMapping("/message/group/{groupId}")
    public void handleGroupMessage(
            @DestinationVariable Integer groupId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("📥 Recibido mensaje para grupo: {}", groupId);

        try {
            // ✅ EXTRAER usuario directamente del mensaje WebSocket
            UserDomain authenticatedUser = extractUserFromMessage(headerAccessor);
            log.info("👤 Mensaje del usuario: ID={}, username={}",
                    authenticatedUser.getId(), authenticatedUser.getUsername());

            // Enriquecer mensaje con datos del usuario CORRECTO
            enrichMessageWithUserData(message, authenticatedUser);
            message.setGroupId(groupId);

            // Persistir el mensaje usando el usuario extraído
            MessageDomain savedMessage = persistMessage(message, "group", authenticatedUser);
            log.debug("💾 Mensaje persistido con ID: {} para usuario: {}",
                    savedMessage.getId(), authenticatedUser.getUsername());

            // Preparar mensaje para envío
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar mensaje al tópico del grupo
            messagingTemplate.convertAndSend("/topic/message/group/" + groupId, message);
            log.debug("📤 Mensaje enviado correctamente al grupo {} por usuario {}",
                    groupId, authenticatedUser.getUsername());

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para grupo " + groupId);
        }
    }

    /**
     * ✅ MISMA LÓGICA para threads
     */
    @MessageMapping("/message/thread/{threadId}")
    public void handleThreadMessage(
            @DestinationVariable Integer threadId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("📥 Recibido mensaje para hilo: {}", threadId);

        try {
            UserDomain authenticatedUser = extractUserFromMessage(headerAccessor);
            log.info("👤 Mensaje del usuario: ID={}, username={}",
                    authenticatedUser.getId(), authenticatedUser.getUsername());

            enrichMessageWithUserData(message, authenticatedUser);
            message.setThreadId(threadId);

            MessageDomain savedMessage = persistMessage(message, "thread", authenticatedUser);
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            messagingTemplate.convertAndSend("/topic/message/thread/" + threadId, message);
            log.debug("📤 Mensaje enviado correctamente al hilo {} por usuario {}",
                    threadId, authenticatedUser.getUsername());

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para hilo " + threadId);
        }
    }

    /**
     * ✅ MISMA LÓGICA para posts
     */
    @MessageMapping("/message/post/{postId}")
    public void handlePostMessage(
            @DestinationVariable Integer postId,
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("📥 Recibido mensaje para publicación: {}", postId);

        try {
            UserDomain authenticatedUser = extractUserFromMessage(headerAccessor);
            log.info("👤 Mensaje del usuario: ID={}, username={}",
                    authenticatedUser.getId(), authenticatedUser.getUsername());

            enrichMessageWithUserData(message, authenticatedUser);
            message.setPostId(postId);

            MessageDomain savedMessage = persistMessage(message, "post", authenticatedUser);
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            messagingTemplate.convertAndSend("/topic/message/post/" + postId, message);
            log.debug("📤 Mensaje enviado correctamente a la publicación {} por usuario {}",
                    postId, authenticatedUser.getUsername());

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para publicación " + postId);
        }
    }

    /**
     * ✅ MISMA LÓGICA para forum
     */
    @MessageMapping("/message/forum")
    public void handleForumMessage(
            @Payload MessageWebSocketDTO message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("📥 Recibido mensaje para foro general");

        try {
            UserDomain authenticatedUser = extractUserFromMessage(headerAccessor);
            log.info("👤 Mensaje del usuario: ID={}, username={}",
                    authenticatedUser.getId(), authenticatedUser.getUsername());

            enrichMessageWithUserData(message, authenticatedUser);

            MessageDomain savedMessage = persistMessage(message, "forum", authenticatedUser);
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            messagingTemplate.convertAndSend("/topic/message/forum", message);
            log.debug("📤 Mensaje enviado correctamente al foro general por usuario {}",
                    authenticatedUser.getUsername());

        } catch (Exception e) {
            handleMessageError(e, headerAccessor, "Error al procesar mensaje para foro general");
        }
    }

    /**
     * ✅ MANTENER: Resto de métodos auxiliares sin cambios
     */
    private void handleMessageError(Exception e, SimpMessageHeaderAccessor headerAccessor, String context) {
        log.error("{}: {}", context, e.getMessage(), e);

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

        if (e instanceof AccessDeniedException) {
            throw (AccessDeniedException) e;
        } else if (e instanceof AuthenticationCredentialsNotFoundException) {
            throw (AuthenticationCredentialsNotFoundException) e;
        } else {
            throw new MessageDeliveryException(context + ": " + e.getMessage(), e);
        }
    }

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

    private void enrichMessageWithUserData(MessageWebSocketDTO message, UserDomain currentUser) {
        message.setUserId(currentUser.getId());
        message.setUsername(currentUser.getUsername());
        message.setUserAvatar(currentUser.getAvatar());
        message.setTimestamp(LocalDateTime.now());
    }
}