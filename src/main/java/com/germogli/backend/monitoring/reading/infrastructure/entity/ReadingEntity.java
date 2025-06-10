package com.germogli.backend.monitoring.reading.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una lectura de sensor.
 * Mapea la tabla sensor_readings en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "ReadingEntity")
@Table(name = "sensor_readings")
public class ReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Integer id;

    @Column(name = "crop_id", nullable = false)
    private Integer cropId;

    @Column(name = "sensor_id", nullable = false)
    private Integer sensorId;

    @Column(name = "reading_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal readingValue;

    @Column(name = "reading_date", nullable = false)
    private LocalDateTime readingDate;
}