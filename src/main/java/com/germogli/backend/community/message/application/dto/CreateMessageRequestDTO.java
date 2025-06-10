package com.germogli.backend.community.message.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un mensaje.
 * Contiene la información necesaria para crear un mensaje.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequestDTO {
    private Integer postId;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    // Opcionales: si el mensaje está relacionado con un hilo o grupo.
    private Integer threadId;
    private Integer groupId;
}
