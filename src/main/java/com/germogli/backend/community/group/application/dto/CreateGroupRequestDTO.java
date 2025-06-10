package com.germogli.backend.community.group.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un grupo.
 * Contiene los datos mínimos requeridos para crear un grupo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequestDTO {
    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String name;

    private String description;
}
