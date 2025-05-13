package com.germogli.backend.monitoring.sensor.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación o actualización de un sensor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorRequestDTO {
    @NotBlank(message = "El tipo de sensor es obligatorio")
    private String sensorType;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unitOfMeasurement;
}