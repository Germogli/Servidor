package com.germogli.backend.education.guides.domain.model;

import com.germogli.backend.education.domain.model.Converter;
import com.germogli.backend.education.guides.infrastructure.entity.GuideEntity;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para una guía educativa.
 * Representa la lógica de negocio y se utiliza en el servicio.
 * Implementa la interfaz Converter para facilitar la conversión entre GuideDomain y GuideEntity.
 */
@Data
@Builder
public class GuideDomain implements Converter<GuideDomain, GuideEntity> {

    private Integer guideId;
    private String title;
    private String description;
    private String pdfUrl;
    private String pdfFileName;
    private LocalDateTime creationDate;
    private ModuleDomain moduleId; // Asociado al módulo al que pertenece la guía

    /**
     * Convierte una entidad GuideEntity en un objeto de dominio GuideDomain.
     *
     * @param entity Entidad de infraestructura a convertir.
     * @return Instancia de GuideDomain con los datos mapeados.
     */
    @Override
    public GuideDomain fromEntity(GuideEntity entity) {
        return GuideDomain.builder()
                .guideId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .pdfUrl(entity.getPdfUrl())
                .pdfFileName(entity.getPdfFileName())
                .creationDate(entity.getCreationDate())
                // Convertir el módulo a dominio; aquí solo asignamos el id, pero se puede expandir si es necesario
                .moduleId(ModuleDomain.builder().moduleId(entity.getModuleId().getId()).build())
                .build();
    }

    /**
     * Método estático que convierte una entidad GuideEntity en un objeto de dominio GuideDomain.
     *
     * @param entity La entidad GuideEntity a convertir.
     * @return Instancia de GuideDomain con los datos mapeados.
     */
    public static GuideDomain fromEntityStatic(GuideEntity entity) {
        return GuideDomain.builder()
                .guideId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .pdfUrl(entity.getPdfUrl())
                .pdfFileName(entity.getPdfFileName())
                .creationDate(entity.getCreationDate())
                .moduleId(ModuleDomain.builder().moduleId(entity.getModuleId().getId()).build())
                .build();
    }

    /**
     * Convierte este objeto de dominio en una entidad GuideEntity.
     *
     * @return Entidad GuideEntity resultante.
     */
    @Override
    public GuideEntity toEntity() {
        return GuideEntity.builder()
                .id(this.guideId)
                .title(this.title)
                .description(this.description)
                .pdfUrl(this.pdfUrl)
                .pdfFileName(this.pdfFileName)
                .creationDate(this.creationDate)
                // Convertir el módulo de dominio a entidad;
                .moduleId(ModuleEntity.builder().id(this.moduleId.getModuleId()).build())
                .build();
    }
}
