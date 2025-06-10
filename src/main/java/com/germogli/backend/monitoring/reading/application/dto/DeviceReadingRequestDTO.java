package com.germogli.backend.monitoring.reading.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para recibir datos de sensores desde dispositivos ESP32.
 * Adaptado para la estructura JSON que envía el ESP32.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceReadingRequestDTO {
    // Nombre del campo como viene del ESP32
    private BigDecimal temperature;
    private BigDecimal humedad;
    private BigDecimal tds;

    // Información opcional del dispositivo
    private BigDecimal batteryLevel;
    private Integer wifiStrength;
}