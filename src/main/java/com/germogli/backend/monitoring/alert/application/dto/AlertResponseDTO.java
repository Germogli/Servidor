package com.germogli.backend.monitoring.alert.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una alerta.
 */
@Data
@Builder
public class AlertResponseDTO {
    private Integer id;
    private Integer cropId;
    private Integer sensorId;
    private String alertMessage;
    private String alertLevel;
    private LocalDateTime alertDatetime;
    private String cropName;
    private String sensorType;
}