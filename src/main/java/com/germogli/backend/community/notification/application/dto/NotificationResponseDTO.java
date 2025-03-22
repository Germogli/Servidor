package com.germogli.backend.community.notification.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para una notificación.
 * Se utiliza para enviar la información de la notificación en las respuestas de la API.
 */
@Data
@Builder
public class NotificationResponseDTO {
    private Integer id;
    private Integer userId;
    private String message;
    private String category;
    private LocalDateTime creationDate;
}
