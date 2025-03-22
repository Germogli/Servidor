package com.germogli.backend.community.comment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para comentarios.
 * Se utiliza para enviar la información del comentario en la respuesta de la API.
 */
@Data
@Builder
public class CommentResponseDTO {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String content;
    private Integer threadId;
    private Integer groupId;
    private LocalDateTime creationDate;
}
