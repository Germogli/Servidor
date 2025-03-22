package com.germogli.backend.community.reaction.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para una reacción.
 * Se utiliza para enviar la información de la reacción en las respuestas de la API.
 */
@Data
@Builder
public class ReactionResponseDTO {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String reactionType;
    private LocalDateTime creationDate;
}
