package com.germogli.backend.monitoring.alert.domain.model;

import com.germogli.backend.monitoring.domain.model.Converter;
import com.germogli.backend.monitoring.alert.infrastructure.entity.AlertEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para una alerta.
 * Representa la información y la lógica de negocio asociada a una alerta
 * generada por condiciones anómalas en los sensores.
 * Implementa Converter para estandarizar la conversión entre AlertEntity y AlertDomain.
 */
@Data
@SuperBuilder
public class AlertDomain implements Converter<AlertDomain, AlertEntity> {
    private Integer id;
    private Integer cropId;
    private Integer sensorId;
    private String alertMessage;
    private String alertLevel;
    private LocalDateTime alertDatetime;

    /**
     * Convierte una entidad AlertEntity en un objeto AlertDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto AlertDomain.
     */
    @Override
    public AlertDomain fromEntity(AlertEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad AlertEntity en un objeto AlertDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto AlertDomain con los datos de la entidad.
     */
    public static AlertDomain fromEntityStatic(AlertEntity entity) {
        return AlertDomain.builder()
                .id(entity.getId())
                .cropId(entity.getCropId())
                .sensorId(entity.getSensorId())
                .alertMessage(entity.getAlertMessage())
                .alertLevel(entity.getAlertLevel())
                .alertDatetime(entity.getAlertDatetime())
                .build();
    }

    /**
     * Convierte este objeto AlertDomain en una entidad AlertEntity para persistencia.
     *
     * @return Objeto AlertEntity con los datos de esta alerta.
     */
    @Override
    public AlertEntity toEntity() {
        return AlertEntity.builder()
                .id(this.id)
                .cropId(this.cropId)
                .sensorId(this.sensorId)
                .alertMessage(this.alertMessage)
                .alertLevel(this.alertLevel)
                .alertDatetime(this.alertDatetime)
                .build();
    }
}