package com.germogli.backend.community.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una notificación.
 * Contiene los datos necesarios para crear una notificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequestDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer userId;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    // Opcional: Categoría (por ejemplo, "post", "thread")
    private String category;
}
