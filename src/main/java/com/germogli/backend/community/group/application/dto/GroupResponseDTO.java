package com.germogli.backend.community.group.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para un grupo.
 * Se utiliza para enviar la información del grupo en las respuestas de la API.
 */
@Data
@Builder
public class GroupResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime creationDate;
}
