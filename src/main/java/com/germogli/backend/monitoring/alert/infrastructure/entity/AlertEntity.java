package com.germogli.backend.monitoring.alert.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una alerta.
 * Mapea la tabla crop_alerts en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "AlertEntity")
@Table(name = "crop_alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Integer id;

    @Column(name = "crop_id", nullable = false)
    private Integer cropId;

    @Column(name = "sensor_id")
    private Integer sensorId;

    @Column(name = "alert_message", nullable = false, columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "alert_level", nullable = false, length = 50)
    private String alertLevel;

    @Column(name = "alert_datetime", nullable = false)
    private LocalDateTime alertDatetime;
}