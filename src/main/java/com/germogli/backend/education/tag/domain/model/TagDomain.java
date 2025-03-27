package com.germogli.backend.education.tag.domain.model;


import com.germogli.backend.education.tag.infrastructure.entity.TagEntity;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Modelo de dominio para una etiqueta.
 * Este modelo representa la lógica de negocio y se utiliza en el servicio.
 */
@Data
@Builder
public class TagDomain {
    private Integer tagId;
    private String tagName;

    // Conversión de entidad a dominio
    public static TagDomain fromEntity(TagEntity entity) {
        return TagDomain.builder()
                .tagId(entity.getId())
                .tagName(entity.getName())
                .build();
    }

    // Conversión de dominio a entidad
    public TagEntity toEntity() {
        return TagEntity.builder()
                .id(this.tagId)
                .name(this.tagName)
                .build();
    }

    // Convierte una lista de entidades TagEntity a una lista de objetos TagDomain
    public static Set<TagDomain> fromEntities(Set<TagEntity> entities) {
        return entities.stream()
                .map(entity -> TagDomain.builder()
                        .tagId(entity.getId())
                        .tagName(entity.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    // Convierte una lista de objetos TagDomain a una lista de entidades TagEntity
    public static Set<TagEntity> toEntities(Set<TagDomain> tagDomains) {
        Set<TagEntity> tagEntities = new HashSet<>();
        for (TagDomain domain : tagDomains) {
            tagEntities.add(domain.toEntity());
        }
        return tagEntities;
    }
}
