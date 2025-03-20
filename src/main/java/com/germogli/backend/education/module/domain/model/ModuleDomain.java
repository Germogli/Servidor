package com.germogli.backend.education.module.domain.model;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Modelo de dominio para el módulo de educación.
 * Este modelo representa la lógica de negocio y se utiliza en el servicio.
 */
@Data
@Builder
public class ModuleDomain {
    private Integer moduleId;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private Set<TagDomain> tags = new HashSet<>();

    // Conversión de entidad a dominio
    public static ModuleDomain fromEntity(ModuleEntity entity) {
        return ModuleDomain.builder()
                .moduleId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .creationDate(entity.getCreationDate())
                .tags(TagDomain.fromEntities(entity.getTags())) // Convierte las entidades de Tags
                .build();
    }

    // Conversión de dominio a entidad
    public ModuleEntity toEntity() {
        return ModuleEntity.builder()
                .id(this.moduleId)
                .title(this.title)
                .description(this.description)
                .creationDate(this.creationDate)
                .tags(TagDomain.toEntities(this.tags)) // Convierte las entidades de Tags
                .build();
    }
}
