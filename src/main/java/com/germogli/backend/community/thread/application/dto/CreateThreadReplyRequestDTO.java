package com.germogli.backend.community.thread.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una respuesta a un hilo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadReplyRequestDTO {
    @NotNull(message = "El ID del thread es obligatorio")
    private Integer threadId;

    @NotBlank(message = "El contenido de la respuesta es obligatorio")
    private String content;
}
