package com.germogli.backend.community.post.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una publicación en la comunidad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {
    /**
     * Tipo de publicación (obligatorio).
     */
    @NotBlank(message = "El tipo de post es obligatorio")
    private String postType;

    /**
     * Contenido de la publicación (obligatorio).
     */
    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    private String multimediaContent;
    private Integer groupId;
    private Integer threadId;
}
