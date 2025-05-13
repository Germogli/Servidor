package com.germogli.backend.monitoring.reading.domain.model;

import com.germogli.backend.monitoring.domain.model.Converter;
import com.germogli.backend.monitoring.reading.infrastructure.entity.ReadingEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para una lectura de sensor.
 * Representa la información y la lógica de negocio asociada a una lectura
 * específica de un sensor en un cultivo.
 * Implementa Converter para estandarizar la conversión entre ReadingEntity y ReadingDomain.
 */
@Data
@SuperBuilder
public class ReadingDomain implements Converter<ReadingDomain, ReadingEntity> {
    private Integer id;
    private Integer cropId;
    private Integer sensorId;
    private BigDecimal readingValue;
    private LocalDateTime readingDate;

    /**
     * Convierte una entidad ReadingEntity en un objeto ReadingDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ReadingDomain.
     */
    @Override
    public ReadingDomain fromEntity(ReadingEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad ReadingEntity en un objeto ReadingDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ReadingDomain con los datos de la entidad.
     */
    public static ReadingDomain fromEntityStatic(ReadingEntity entity) {
        return ReadingDomain.builder()
                .id(entity.getId())
                .cropId(entity.getCropId())
                .sensorId(entity.getSensorId())
                .readingValue(entity.getReadingValue())
                .readingDate(entity.getReadingDate())
                .build();
    }

    /**
     * Convierte este objeto ReadingDomain en una entidad ReadingEntity para persistencia.
     *
     * @return Objeto ReadingEntity con los datos de esta lectura.
     */
    @Override
    public ReadingEntity toEntity() {
        return ReadingEntity.builder()
                .id(this.id)
                .cropId(this.cropId)
                .sensorId(this.sensorId)
                .readingValue(this.readingValue)
                .readingDate(this.readingDate)
                .build();
    }
}