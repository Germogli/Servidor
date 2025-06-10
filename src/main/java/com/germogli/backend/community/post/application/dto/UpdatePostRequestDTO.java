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
     * - Si es null: NO se actualiza el campo multimedia (se mantiene el valor existente)
     * - Si es cadena vacía ("" o "   "): se ELIMINA el archivo multimedia existente
     * - Si tiene valor: se MANTIENE el archivo multimedia actual (no se usa en la lógica actual)
     * - Si se envía un archivo (MultipartFile): se REEMPLAZA el archivo multimedia
     */
    private String multimediaContent;
}