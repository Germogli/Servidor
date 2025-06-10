package com.germogli.backend.community.message.infrastructure.cache;

import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caché para almacenar los mensajes recientes y reducir consultas a la base de datos.
 * Utiliza una estructura de datos en memoria para mantener los últimos mensajes por contexto.
 */
@Component
public class MessageCache {

    // Capacidad máxima de mensajes por contexto
    private static final int MAX_CACHE_SIZE = 100;

    // Map estructura: [tipo_contexto:id_contexto] -> List<MessageResponseDTO>
    private final Map<String, List<MessageResponseDTO>> messageCache = new ConcurrentHashMap<>();

    /**
     * Genera una clave de caché basada en el tipo de contexto y su ID.
     *
     * @param contextType Tipo de contexto (group, thread, post, forum)
     * @param contextId   ID del contexto (puede ser null para forum)
     * @return Clave para el caché
     */
    private String generateCacheKey(String contextType, Integer contextId) {
        return contextType + ":" + (contextId != null ? contextId : "general");
    }

    /**
     * Añade un mensaje al caché para un contexto específico.
     *
     * @param contextType Tipo de contexto
     * @param contextId   ID del contexto
     * @param message     Mensaje a almacenar
     */
    public synchronized void addMessage(String contextType, Integer contextId, MessageResponseDTO message) {
        String cacheKey = generateCacheKey(contextType, contextId);

        // Obtener o crear la lista de mensajes para este contexto
        List<MessageResponseDTO> messages = messageCache.computeIfAbsent(cacheKey, k -> new ArrayList<>());

        // Añadir el mensaje al inicio de la lista (ordenados por más recientes primero)
        messages.add(0, message);

        // Mantener el tamaño máximo de la caché
        if (messages.size() > MAX_CACHE_SIZE) {
            messages.remove(messages.size() - 1);
        }
    }

    /**
     * Obtiene los mensajes más recientes para un contexto específico.
     *
     * @param contextType Tipo de contexto
     * @param contextId   ID del contexto
     * @param limit       Número máximo de mensajes a recuperar
     * @return Lista de mensajes recientes
     */
    public List<MessageResponseDTO> getRecentMessages(String contextType, Integer contextId, int limit) {
        String cacheKey = generateCacheKey(contextType, contextId);
        List<MessageResponseDTO> messages = messageCache.getOrDefault(cacheKey, new ArrayList<>());

        // Devolver solo hasta el límite especificado
        return messages.size() <= limit ? new ArrayList<>(messages) :
                new ArrayList<>(messages.subList(0, limit));
    }

    /**
     * Elimina un mensaje específico del caché.
     *
     * @param messageId ID del mensaje a eliminar
     */
    public synchronized void removeMessage(Integer messageId) {
        // Buscar y eliminar el mensaje en todas las listas del caché
        messageCache.values().forEach(messages ->
                messages.removeIf(msg -> msg.getId().equals(messageId))
        );
    }

    /**
     * Limpia todos los mensajes del caché para un contexto específico.
     *
     * @param contextType Tipo de contexto
     * @param contextId   ID del contexto
     */
    public synchronized void clearCache(String contextType, Integer contextId) {
        String cacheKey = generateCacheKey(contextType, contextId);
        messageCache.remove(cacheKey);
    }
}