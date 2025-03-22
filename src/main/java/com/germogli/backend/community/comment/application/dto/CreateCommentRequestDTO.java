package com.germogli.backend.community.comment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un comentario.
 * Contiene la información necesaria para crear un comentario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequestDTO {
    @NotNull(message = "El ID del post es obligatorio")
    private Integer postId;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    // Opcionales: si el comentario está relacionado con un hilo o grupo.
    private Integer threadId;
    private Integer groupId;
}
