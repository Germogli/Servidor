package com.germogli.backend.monitoring.reading.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para el envío por lotes de lecturas de sensores.
 * Utilizado por dispositivos IoT para enviar múltiples lecturas en una sola solicitud.
 */
@Data
public class ReadingBatchRequestDTO {
    @NotNull(message = "El ID del dispositivo es obligatorio")
    private Integer deviceId;

    @NotNull(message = "El ID del cultivo es obligatorio")
    private Integer cropId;

    private LocalDateTime timestamp;

    @NotEmpty(message = "Se requiere al menos una lectura")
    @Valid
    private List<ReadingSensorDTO> readings;

    private Float batteryLevel;

    private Integer wifiStrength;

    /**
     * DTO anidado para cada lectura individual de sensor dentro del lote.
     */
    @Data
    public static class ReadingSensorDTO {
        @NotNull(message = "El ID del sensor es obligatorio")
        private Integer sensorId;

        @NotNull(message = "El valor de la lectura es obligatorio")
        private Double value;

        private String unit;
    }
}