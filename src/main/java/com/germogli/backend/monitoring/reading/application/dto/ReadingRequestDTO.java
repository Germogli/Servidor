package com.germogli.backend.monitoring.reading.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la creación de una lectura de sensor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingRequestDTO {
    @NotNull(message = "El ID del cultivo es obligatorio")
    private Integer cropId;

    @NotNull(message = "El ID del sensor es obligatorio")
    private Integer sensorId;

    @NotNull(message = "El valor de la lectura es obligatorio")
    private BigDecimal readingValue;

    private LocalDateTime readingDate;
}