package com.germogli.backend.monitoring.sensor.domain.model;

import com.germogli.backend.monitoring.domain.model.Converter;
import com.germogli.backend.monitoring.sensor.infrastructure.entity.SensorEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para un sensor.
 * Representa la información y la lógica de negocio asociada a un sensor para monitoreo
 * de cultivos hidropónicos. Implementa Converter para estandarizar la conversión
 * entre SensorEntity y SensorDomain.
 */
@Data
@SuperBuilder
public class SensorDomain implements Converter<SensorDomain, SensorEntity> {
    private Integer id;
    private String sensorType;
    private String unitOfMeasurement;

    /**
     * Convierte una entidad SensorEntity en un objeto SensorDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto SensorDomain.
     */
    @Override
    public SensorDomain fromEntity(SensorEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad SensorEntity en un objeto SensorDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto SensorDomain con los datos de la entidad.
     */
    public static SensorDomain fromEntityStatic(SensorEntity entity) {
        return SensorDomain.builder()
                .id(entity.getId())
                .sensorType(entity.getSensorType())
                .unitOfMeasurement(entity.getUnitOfMeasurement())
                .build();
    }

    /**
     * Convierte este objeto SensorDomain en una entidad SensorEntity para persistencia.
     *
     * @return Objeto SensorEntity con los datos de este sensor.
     */
    @Override
    public SensorEntity toEntity() {
        return SensorEntity.builder()
                .id(this.id)
                .sensorType(this.sensorType)
                .unitOfMeasurement(this.unitOfMeasurement)
                .build();
    }
}