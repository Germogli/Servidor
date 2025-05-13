package com.germogli.backend.monitoring.crop.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación o actualización de un cultivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRequestDTO {
    @NotBlank(message = "El nombre del cultivo es obligatorio")
    private String cropName;

    private String cropType;
}