package com.germogli.backend.monitoring.sensor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clave compuesta para la entidad CropSensorEntity.
 * Representa la clave primaria compuesta de la tabla crop_sensors.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropSensorId implements Serializable {

    @Column(name = "crop_id")
    private Integer cropId;

    @Column(name = "sensor_id")
    private Integer sensorId;
}