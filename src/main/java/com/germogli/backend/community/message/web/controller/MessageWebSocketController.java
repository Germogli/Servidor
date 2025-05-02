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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Controlador WebSocket para la gestión de mensajes en tiempo real.
 * Maneja los mensajes recibidos por WebSocket y los distribuye a los tópicos correspondientes.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class MessageWebSocketController {

    private final MessageDomainService messageDomainService;
    private final CommunitySharedService sharedService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Maneja mensajes enviados a grupos específicos.
     *
     * @param groupId ID del grupo
     * @param message Mensaje enviado
     */
    @MessageMapping("/message/group/{groupId}")
    public void handleGroupMessage(@DestinationVariable Integer groupId, @Payload MessageWebSocketDTO message) {
        log.info("Recibido mensaje para grupo: {}, contenido: {}", groupId, message.getContent());
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        try {
            // Establecer datos del usuario
            enrichMessageWithUserData(message, currentUser);

            // Persistir el mensaje
            log.info("Intentando persistir mensaje: usuario={}, grupo={}, contenido={}",
                    currentUser.getId(), groupId, message.getContent());
            MessageDomain savedMessage = persistMessage(message, "group");
            log.info("Mensaje persistido con ID: {}", savedMessage.getId());

            // Enriquecer el mensaje con datos adicionales para enviar
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar el mensaje al tópico del grupo
            log.info("Enviando mensaje al tópico: /topic/message/group/{}", groupId);
            messagingTemplate.convertAndSend("/topic/message/group/" + groupId, message);
            log.info("Mensaje enviado correctamente");
        } catch (Exception e) {
            log.error("Error al procesar mensaje de grupo: {}", e.getMessage(), e);
            throw new MessageDeliveryException("Error al entregar mensaje al grupo: " + e.getMessage(), e);
        }
    }

    /**
     * Maneja mensajes enviados a hilos específicos.
     *
     * @param threadId ID del hilo
     * @param message  Mensaje enviado
     */
    @MessageMapping("/message/thread/{threadId}")
    public void handleThreadMessage(@DestinationVariable Integer threadId, @Payload MessageWebSocketDTO message) {
        log.info("Recibido mensaje para hilo: {}", threadId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        try {
            // Establecer datos del usuario
            enrichMessageWithUserData(message, currentUser);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "thread");

            // Enriquecer el mensaje con datos adicionales para enviar
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar el mensaje al tópico del hilo
            messagingTemplate.convertAndSend("/topic/message/thread/" + threadId, message);
        } catch (Exception e) {
            log.error("Error al procesar mensaje de hilo: {}", e.getMessage(), e);
            throw new MessageDeliveryException("Error al entregar mensaje al hilo: " + e.getMessage(), e);
        }
    }

    /**
     * Maneja mensajes enviados a publicaciones específicas.
     *
     * @param postId  ID de la publicación
     * @param message Mensaje enviado
     */
    @MessageMapping("/message/post/{postId}")
    public void handlePostMessage(@DestinationVariable Integer postId, @Payload MessageWebSocketDTO message) {
        log.info("Recibido mensaje para publicación: {}", postId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        try {
            // Establecer datos del usuario
            enrichMessageWithUserData(message, currentUser);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "post");

            // Enriquecer el mensaje con datos adicionales para enviar
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar el mensaje al tópico de la publicación
            messagingTemplate.convertAndSend("/topic/message/post/" + postId, message);
        } catch (Exception e) {
            log.error("Error al procesar mensaje de publicación: {}", e.getMessage(), e);
            throw new MessageDeliveryException("Error al entregar mensaje a la publicación: " + e.getMessage(), e);
        }
    }

    /**
     * Maneja mensajes enviados al foro general.
     *
     * @param message Mensaje enviado
     */
    @MessageMapping("/message/forum")
    public void handleForumMessage(@Payload MessageWebSocketDTO message) {
        log.info("Recibido mensaje para el foro general");
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        try {
            // Establecer datos del usuario
            enrichMessageWithUserData(message, currentUser);

            // Persistir el mensaje
            MessageDomain savedMessage = persistMessage(message, "forum");

            // Enriquecer el mensaje con datos adicionales para enviar
            message.setId(savedMessage.getId());
            message.setTimestamp(savedMessage.getCreationDate());

            // Enviar el mensaje al tópico del foro general
            messagingTemplate.convertAndSend("/topic/message/forum", message);
        } catch (Exception e) {
            log.error("Error al procesar mensaje del foro general: {}", e.getMessage(), e);
            throw new MessageDeliveryException("Error al entregar mensaje al foro general: " + e.getMessage(), e);
        }
    }

    /**
     * Enriquece el mensaje con los datos del usuario autenticado.
     *
     * @param message     Mensaje a enriquecer
     * @param currentUser Usuario autenticado
     */
    private void enrichMessageWithUserData(MessageWebSocketDTO message, UserDomain currentUser) {
        message.setUserId(currentUser.getId());
        message.setUsername(currentUser.getUsername());
        message.setUserAvatar(currentUser.getAvatar());
        message.setTimestamp(LocalDateTime.now());
    }

    /**
     * Persiste el mensaje en la base de datos.
     *
     * @param message     Mensaje a persistir
     * @param contextType Tipo de contexto
     * @return Mensaje persistido
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