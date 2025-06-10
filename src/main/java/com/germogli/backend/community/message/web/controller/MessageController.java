package com.germogli.backend.community.message.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.message.application.dto.CreateMessageRequestDTO;
import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.service.MessageDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de mensajes en Community.
 * Proporciona endpoints para crear, obtener, listar y eliminar mensajes.
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MessageController {

    private final MessageDomainService messageDomainService;

    /**
     * Endpoint para crear un nuevo mensaje.
     *
     * @param request DTO con los datos para crear el mensaje.
     * @return Respuesta API con el mensaje creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<MessageResponseDTO>> createMessage(@Valid @RequestBody CreateMessageRequestDTO request) {
        MessageDomain message = messageDomainService.createMessage(request);
        return ResponseEntity.ok(ApiResponseDTO.<MessageResponseDTO>builder()
                .message("Mensaje creado correctamente")
                .data(messageDomainService.toResponse(message))
                .build());
    }

    /**
     * Endpoint para obtener un mensaje por su ID.
     *
     * @param id Identificador del mensaje.
     * @return Respuesta API con el mensaje encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<MessageResponseDTO>> getMessageById(@PathVariable Integer id) {
        MessageDomain message = messageDomainService.getMessageById(id);
        return ResponseEntity.ok(ApiResponseDTO.<MessageResponseDTO>builder()
                .message("Mensaje recuperado correctamente")
                .data(messageDomainService.toResponse(message))
                .build());
    }

    /**
     * Endpoint para listar todos los mensajes.
     *
     * @return Respuesta API con la lista de mensajes.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<MessageResponseDTO>>> getAllMessages() {
        List<MessageResponseDTO> messages = messageDomainService.toResponseList(messageDomainService.getAllMessages());
        return ResponseEntity.ok(ApiResponseDTO.<List<MessageResponseDTO>>builder()
                .message("Mensajes recuperados correctamente")
                .data(messages)
                .build());
    }

    /**
     * Endpoint para eliminar un mensaje.
     * Solo el propietario, un administrador o un moderador pueden eliminar el mensaje.
     *
     * @param id Identificador del mensaje a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@messageSecurity.canDelete(#id, principal)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMessage(@PathVariable Integer id) {
        messageDomainService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Mensaje eliminado correctamente")
                .build());
    }
}
