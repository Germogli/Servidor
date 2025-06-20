package com.germogli.backend.community.message.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import com.germogli.backend.community.message.domain.service.MessageChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controlador REST para obtener mensajes históricos.
 * Ofrece endpoints para recuperar mensajes anteriores en caso de reconexión o paginación.
 */
@RestController
@RequestMapping("/messages/history")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Slf4j
public class MessageHistoryController {

    private final MessageChatService messageChatService;

    /**
     * Obtiene mensajes históricos para un contexto específico con paginación.
     *
     * @param contextType Tipo de contexto (group, thread, post, forum)
     * @param contextId   ID del contexto
     * @param limit       Número máximo de mensajes a recuperar (por defecto 50)
     * @param offset      Desplazamiento para paginación (por defecto 0)
     * @return Lista de mensajes históricos
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<MessageResponseDTO>>> getMessageHistory(
            @RequestParam String contextType,
            @RequestParam(required = false) Integer contextId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        log.info("Recibida petición de historial para {}: {}, limit: {}, offset: {}",
                contextType, contextId, limit, offset);

        try {
            List<MessageResponseDTO> messages = messageChatService.getMessagesByContext(
                    contextType, contextId, limit, offset);

            log.info("Recuperados {} mensajes del historial", messages.size());

            return ResponseEntity.ok(ApiResponseDTO.<List<MessageResponseDTO>>builder()
                    .message("Historial de mensajes recuperado correctamente")
                    .data(messages)
                    .build());
        } catch (Exception e) {
            log.error("Error al recuperar mensajes: ", e);

            // Proporcionar una respuesta vacía con un mensaje claro
            return ResponseEntity.ok(ApiResponseDTO.<List<MessageResponseDTO>>builder()
                    .message("No se pudo cargar el historial debido a un error interno: " + e.getMessage())
                    .data(Collections.emptyList())
                    .build());
        }
    }
}