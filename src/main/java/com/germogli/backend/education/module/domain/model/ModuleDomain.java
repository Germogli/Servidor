package com.germogli.backend.education.module.domain.model;

import com.germogli.backend.education.domain.model.Converter;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Modelo de dominio para el módulo de educación.
 * Representa la lógica de negocio y se utiliza en el servicio.
 *
 * Además, implementa la interfaz Converter para facilitar la conversión
 * entre ModuleDomain y ModuleEntity.
 */
@Data
@Builder
public class ModuleDomain implements Converter<ModuleDomain, ModuleEntity> {

    private Integer moduleId;
    private String title;
    private String description;
    private LocalDateTime creationDate = LocalDateTime.now();
    private Set<TagDomain> tags = new HashSet<>();

    /**
     * Implementación de la conversión desde la entidad a dominio.
     *
     * @param entity Entidad de infraestructura a convertir.
     * @return Instancia de ModuleDomain resultante.
     */
    @Override
    public ModuleDomain fromEntity(ModuleEntity entity) {
        return ModuleDomain.builder()
                .moduleId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .creationDate(entity.getCreationDate())
                .tags(TagDomain.fromEntities(entity.getTags()))
                .build();
    }

    /**
     * Método estático que convierte una entidad de tipo ModuleEntity en un objeto de dominio ModuleDomain.
     * Este método permite usar la referencia de método, por ejemplo, ModuleDomain::fromEntityStatic.
     *
     * @param entity La entidad ModuleEntity que se desea convertir.
     * @return Una instancia de ModuleDomain con los datos mapeados desde la entidad.
     */
    public static ModuleDomain fromEntityStatic(ModuleEntity entity) {
        return ModuleDomain.builder()
                .moduleId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .creationDate(entity.getCreationDate())
                .tags(TagDomain.fromEntities(entity.getTags()))
                .build();
    }

    /**
     * Implementación de la conversión desde el modelo de dominio a la entidad.
     *
     * @return Entidad ModuleEntity resultante.
     */
    @Override
    public ModuleEntity toEntity() {
        return ModuleEntity.builder()
                .id(this.moduleId)
                .title(this.title)
                .description(this.description)
                .creationDate(this.creationDate)
                .tags(TagDomain.toEntities(this.tags))
                .build();
    }

    /**
     * Método de conveniencia para convertir una lista de entidades a modelos de dominio.
     *
     * @param entities Lista de ModuleEntity.
     * @return Lista de ModuleDomain.
     */
    public static List<ModuleDomain> fromEntities(List<ModuleEntity> entities) {
        return entities.stream()
                .map(entity -> ModuleDomain.builder()
                        .moduleId(entity.getId())
                        .title(entity.getTitle())
                        .description(entity.getDescription())
                        .creationDate(entity.getCreationDate())
                        .tags(TagDomain.fromEntities(entity.getTags()))
                        .build())
                .collect(Collectors.toList());
    }
}
