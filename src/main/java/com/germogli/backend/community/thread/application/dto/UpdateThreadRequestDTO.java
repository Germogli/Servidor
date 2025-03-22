package com.germogli.backend.community.thread.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un hilo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateThreadRequestDTO {
    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;
}
