package com.germogli.backend.monitoring.sensor.infrastructure.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

/**
 * Entidad JPA que representa la relación muchos a muchos entre Crops y Sensors.
 * Mapea la tabla crop_sensors en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CropSensorEntity")
@Table(name = "crop_sensors")
public class CropSensorEntity {

    @EmbeddedId
    private CropSensorId id;
}