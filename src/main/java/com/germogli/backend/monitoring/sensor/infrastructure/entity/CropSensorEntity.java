package com.germogli.backend.monitoring.sensor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa la relación muchos a muchos entre Crops y Sensors.
 * Mapea la tabla crop_sensors en la base de datos.
 * Incluye umbrales personalizados para cada combinación cultivo-sensor.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CropSensorEntity")
@Table(name = "crop_sensors")
public class CropSensorEntity {

    @EmbeddedId
    private CropSensorId id;

    @Column(name = "min_threshold", precision = 10, scale = 2)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", precision = 10, scale = 2)
    private BigDecimal maxThreshold;
}