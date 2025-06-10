package com.germogli.backend.monitoring.alert.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa el historial de alertas.
 * Mapea la tabla alert_history en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "AlertHistoryEntity")
@Table(name = "alert_history")
public class AlertHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "crop_id", nullable = false)
    private Integer cropId;

    @Column(name = "alert_type", nullable = false, length = 100)
    private String alertType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "alert_level", nullable = false, length = 50)
    private String alertLevel;

    @Column(name = "generation_datetime", nullable = false)
    private LocalDateTime generationDatetime;

    @Column(name = "alert_status", nullable = false, length = 50)
    private String alertStatus;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "sensor_id")
    private Integer sensorId;
}