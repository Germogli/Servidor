package com.germogli.backend.monitoring.sensor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la creación de un sensor con umbrales que será asociado directamente a un cultivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorWithThresholdsRequestDTO {
    @NotBlank(message = "El tipo de sensor es obligatorio")
    private String sensorType;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unitOfMeasurement;

    @NotNull(message = "El valor mínimo es obligatorio")
    private BigDecimal minThreshold;

    @NotNull(message = "El valor máximo es obligatorio")
    private BigDecimal maxThreshold;
}