package com.germogli.backend.monitoring.alert.domain.model;

import com.germogli.backend.monitoring.domain.model.Converter;
import com.germogli.backend.monitoring.alert.infrastructure.entity.AlertHistoryEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para el historial de alertas.
 * Representa la información y la lógica de negocio asociada al historial
 * de alertas generadas por los sensores.
 * Implementa Converter para estandarizar la conversión entre AlertHistoryEntity y AlertHistoryDomain.
 */
@Data
@SuperBuilder
public class AlertHistoryDomain implements Converter<AlertHistoryDomain, AlertHistoryEntity> {
    private Integer id;
    private Integer userId;
    private Integer cropId;
    private String alertType;
    private String message;
    private String alertLevel;
    private LocalDateTime generationDatetime;
    private String alertStatus;
    private LocalDateTime resolutionDate;
    private String comments;
    private Integer sensorId;

    /**
     * Convierte una entidad AlertHistoryEntity en un objeto AlertHistoryDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto AlertHistoryDomain.
     */
    @Override
    public AlertHistoryDomain fromEntity(AlertHistoryEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad AlertHistoryEntity en un objeto AlertHistoryDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto AlertHistoryDomain con los datos de la entidad.
     */
    public static AlertHistoryDomain fromEntityStatic(AlertHistoryEntity entity) {
        return AlertHistoryDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .cropId(entity.getCropId())
                .alertType(entity.getAlertType())
                .message(entity.getMessage())
                .alertLevel(entity.getAlertLevel())
                .generationDatetime(entity.getGenerationDatetime())
                .alertStatus(entity.getAlertStatus())
                .resolutionDate(entity.getResolutionDate())
                .comments(entity.getComments())
                .sensorId(entity.getSensorId())
                .build();
    }

    /**
     * Convierte este objeto AlertHistoryDomain en una entidad AlertHistoryEntity para persistencia.
     *
     * @return Objeto AlertHistoryEntity con los datos de este historial de alerta.
     */
    @Override
    public AlertHistoryEntity toEntity() {
        return AlertHistoryEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .cropId(this.cropId)
                .alertType(this.alertType)
                .message(this.message)
                .alertLevel(this.alertLevel)
                .generationDatetime(this.generationDatetime)
                .alertStatus(this.alertStatus)
                .resolutionDate(this.resolutionDate)
                .comments(this.comments)
                .sensorId(this.sensorId)
                .build();
    }
}