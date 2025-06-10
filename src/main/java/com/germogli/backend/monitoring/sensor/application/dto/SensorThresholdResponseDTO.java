package com.germogli.backend.monitoring.sensor.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de respuesta para los umbrales de un sensor asociado a un cultivo.
 * Contiene la información completa del sensor y sus umbrales configurados.
 */
@Data
@Builder
public class SensorThresholdResponseDTO {
    private Integer sensorId;
    private String sensorType;
    private String unitOfMeasurement;
    private BigDecimal minThreshold;
    private BigDecimal maxThreshold;
}
