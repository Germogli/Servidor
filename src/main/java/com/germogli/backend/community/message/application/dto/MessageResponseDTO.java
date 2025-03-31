package com.germogli.backend.community.message.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para mensajes.
 * Se utiliza para enviar la información del mensaje en la respuesta de la API.
 */
@Data
@Builder
public class MessageResponseDTO {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String content;
    private Integer threadId;
    private Integer groupId;
    private LocalDateTime creationDate;
}
