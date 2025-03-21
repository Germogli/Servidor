package com.germogli.backend.education.tag.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una etiqueta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequestDTO {
    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    private String name;
    private Integer id;
}
