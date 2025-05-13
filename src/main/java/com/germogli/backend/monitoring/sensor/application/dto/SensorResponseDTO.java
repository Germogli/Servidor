package com.germogli.backend.monitoring.sensor.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para un sensor.
 */
@Data
@Builder
public class SensorResponseDTO {
    private Integer id;
    private String sensorType;
    private String unitOfMeasurement;
}