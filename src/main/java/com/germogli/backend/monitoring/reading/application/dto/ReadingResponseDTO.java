package com.germogli.backend.monitoring.reading.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para una lectura de sensor.
 */
@Data
@Builder
public class ReadingResponseDTO {
    private Integer id;
    private Integer cropId;
    private Integer sensorId;
    private BigDecimal readingValue;
    private LocalDateTime readingDate;
    private String sensorType;
    private String unitOfMeasurement;
}