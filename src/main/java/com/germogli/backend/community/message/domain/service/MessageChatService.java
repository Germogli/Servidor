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

    private final MessageDomainRepository messageRepository;
    private final MessageContextRepository messageContextRepository;
    private final MessageDomainService messageDomainService;
    private final MessageCache messageCache;

    /**
     * Obtiene mensajes históricos para un contexto específico.
     * Intenta primero obtener los mensajes desde la caché para mejorar el rendimiento.
     * Si no hay datos en caché, consulta la base de datos.
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

        try {
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

            log.info("Recuperados {} mensajes de la base de datos", messageDomains.size());

            // Convertir a DTOs
            List<MessageResponseDTO> messages = messageDomains.stream()
                    .map(messageDomainService::toResponse)
                    .collect(Collectors.toList());

            // Si es la primera página, actualizar caché
            if (offset == 0 && !messages.isEmpty()) {
                messages.forEach(msg -> messageCache.addMessage(contextType, contextId, msg));
                log.debug("Caché actualizada con {} mensajes", messages.size());
            }

            return messages;
        } catch (Exception e) {
            log.error("Error al obtener mensajes por contexto: {}", e.getMessage(), e);
            // Para propósitos de depuración, registrar detalles adicionales
            if (e.getCause() != null) {
                log.error("Causa subyacente: {}", e.getCause().getMessage());
            }
            // Propagar la excepción para que sea manejada por el controlador
            throw e;
        }
    }
}