package com.germogli.backend.monitoring.crop.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un cultivo.
 */
@Data
@Builder
public class CropResponseDTO {
    private Integer id;
    private Integer userId;
    private String cropName;
    private String cropType;
    private LocalDateTime startDate;
}