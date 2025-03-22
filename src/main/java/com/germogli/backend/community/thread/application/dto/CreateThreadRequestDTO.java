package com.germogli.backend.community.thread.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un hilo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadRequestDTO {
    @NotNull(message = "El ID del grupo es obligatorio")
    private Integer groupId;

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;
}
