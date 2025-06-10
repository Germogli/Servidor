package com.germogli.backend.monitoring.crop.domain.model;

import com.germogli.backend.monitoring.crop.infrastructure.entity.CropEntity;
import com.germogli.backend.monitoring.domain.model.Converter;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para un cultivo.
 * Representa la información y la lógica de negocio asociada a un cultivo hidropónico.
 * Implementa Converter para estandarizar la conversión entre CropEntity y CropDomain.
 */
@Data
@SuperBuilder
public class CropDomain implements Converter<CropDomain, CropEntity> {
    private Integer id;
    private Integer userId;
    private String cropName;
    private String cropType;
    private LocalDateTime startDate;

    /**
     * Convierte una entidad CropEntity en un objeto CropDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto CropDomain.
     */
    @Override
    public CropDomain fromEntity(CropEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad CropEntity en un objeto CropDomain.
     *
     * @param entity Entidad a convertir.
     * @return Objeto CropDomain con los datos de la entidad.
     */
    public static CropDomain fromEntityStatic(CropEntity entity) {
        return CropDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .cropName(entity.getCropName())
                .cropType(entity.getCropType())
                .startDate(entity.getStartDate())
                .build();
    }

    /**
     * Convierte este objeto CropDomain en una entidad CropEntity para persistencia.
     *
     * @return Objeto CropEntity con los datos de este cultivo.
     */
    @Override
    public CropEntity toEntity() {
        return CropEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .cropName(this.cropName)
                .cropType(this.cropType)
                .startDate(this.startDate)
                .build();
    }
}