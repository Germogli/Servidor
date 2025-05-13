package com.germogli.backend.monitoring.crop.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un cultivo.
 * Mapea la tabla crops en la base de datos.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CropEntity")
@Table(name = "crops")
public class CropEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(name = "crop_type", length = 100)
    private String cropType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
}