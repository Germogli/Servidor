// src/main/java/com/germogli/backend/community/message/domain/repository/MessageContextRepository.java
package com.germogli.backend.community.message.domain.repository;

import com.germogli.backend.community.message.domain.model.MessageDomain;
import java.util.List;

/**
 * Interfaz para operaciones de persistencia contextual de mensajes.
 */
public interface MessageContextRepository {

    /**
     * Obtiene los mensajes de un contexto específico, ordenados por fecha.
     *
     * @param contextType Tipo de contexto (group, thread, post, forum)
     * @param contextId   ID del contexto
     * @param limit       Máximo número de mensajes a obtener
     * @param offset      Desplazamiento para paginación
     * @return Lista de mensajes del contexto especificado
     */
    List<MessageDomain> findMessagesByContext(String contextType, Integer contextId, int limit, int offset);
}