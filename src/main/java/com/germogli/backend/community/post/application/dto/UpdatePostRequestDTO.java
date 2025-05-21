package com.germogli.backend.community.post.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar una publicación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequestDTO {
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

    /**
     * Contenido multimedia.
     * Si es null, significa que se desea eliminar el archivo multimedia existente.
     * Si tiene valor, se mantendrá el archivo multimedia actual o se sustituirá por uno nuevo.
     */
    private String multimediaContent;
}