package com.germogli.backend.monitoring.sensor.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

/**
 * Entidad JPA que representa un sensor.
 * Mapea la tabla sensors en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "SensorEntity")
@Table(name = "sensors")
public class SensorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Integer id;

    @Column(name = "sensor_type", nullable = false, length = 100)
    private String sensorType;

    @Column(name = "unit_of_measurement", nullable = false, length = 50)
    private String unitOfMeasurement;
}