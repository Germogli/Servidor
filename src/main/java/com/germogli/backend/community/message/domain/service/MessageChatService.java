package com.germogli.backend.community.message.domain.service;

import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import com.germogli.backend.community.message.application.dto.MessageWebSocketDTO;
import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.repository.MessageContextRepository;
import com.germogli.backend.community.message.domain.repository.MessageDomainRepository;
import com.germogli.backend.community.message.infrastructure.cache.MessageCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio especializado para la gestión de mensajes de chat en tiempo real.
 * Complementa el MessageDomainService con funcionalidades específicas para chat.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageChatService {

    private final MessageDomainService messageDomainService;
    private final MessageDomainRepository messageRepository;
    private final MessageContextRepository messageContextRepository;
    private final CommunitySharedService sharedService;
    private final MessageCache messageCache;

    /**
     * Obtiene mensajes históricos para un contexto específico.
     *
     * @param contextType Tipo de contexto (group, thread, post, forum)
     * @param contextId   ID del contexto
     * @param limit       Número máximo de mensajes a recuperar
     * @param offset      Desplazamiento para paginación
     * @return Lista de mensajes para el contexto solicitado
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getMessagesByContext(String contextType, Integer contextId, int limit, int offset) {
        log.info("Buscando mensajes para contexto: {}, ID: {}, limit: {}, offset: {}",
                contextType, contextId, limit, offset);

        // Intentar obtener mensajes desde la caché para solicitudes iniciales
        if (offset == 0 && limit <= 50) {
            List<MessageResponseDTO> cachedMessages = messageCache.getRecentMessages(contextType, contextId, limit);
            if (!cachedMessages.isEmpty()) {
                log.info("Devolviendo {} mensajes desde caché", cachedMessages.size());
                return cachedMessages;
            }
        }

        // Si no hay en caché o se solicitan más mensajes, obtener de la base de datos
        List<MessageDomain> messageDomains = messageContextRepository.findMessagesByContext(
                contextType, contextId, limit, offset);

        // Convertir a DTOs
        List<MessageResponseDTO> messages = messageDomains.stream()
                .map(messageDomainService::toResponse)
                .collect(Collectors.toList());

        // Si es la primera página, actualizar caché
        if (offset == 0 && !messages.isEmpty()) {
            messages.forEach(msg -> messageCache.addMessage(contextType, contextId, msg));
        }

        return messages;
    }

    /**
     * Convierte un MessageDomain a MessageWebSocketDTO para envío por WebSocket.
     *
     * @param message  Mensaje de dominio
     * @param username Nombre de usuario
     * @param avatar   Avatar del usuario
     * @return DTO para WebSocket
     */
    public MessageWebSocketDTO toWebSocketDTO(MessageDomain message, String username, String avatar) {
        return MessageWebSocketDTO.builder()
                .id(message.getId())
                .userId(message.getUserId())
                .username(username)
                .userAvatar(avatar)
                .content(message.getContent())
                .postId(message.getPostId())
                .threadId(message.getThreadId())
                .groupId(message.getGroupId())
                .timestamp(message.getCreationDate())
                .messageType("CHAT")
                .build();
    }
}