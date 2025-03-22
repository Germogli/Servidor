package com.germogli.backend.community.group.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.group.infrastructure.entity.GroupEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para un grupo de la comunidad.
 * Representa la lógica y los datos asociados a un grupo.
 * Implementa la interfaz Converter para estandarizar la conversión entre
 * la entidad (GroupEntity) y el modelo de dominio.
 */
@Data
@SuperBuilder
public class GroupDomain extends BaseCommunityResource implements Converter<GroupDomain, GroupEntity> {
    private Integer id;
    private String name;
    private String description;

    /**
     * Método de instancia requerido por la interfaz Converter.
     * Delegamos en el método estático para permitir el uso de referencias.
     *
     * @param entity Entidad a convertir.
     * @return Objeto GroupDomain con los datos de la entidad.
     */
    @Override
    public GroupDomain fromEntity(GroupEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad GroupEntity en un objeto GroupDomain.
     * Permite utilizar la referencia de método: GroupDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto GroupDomain con los datos de la entidad.
     */
    public static GroupDomain fromEntityStatic(GroupEntity entity) {
        return GroupDomain.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto GroupDomain en una entidad GroupEntity para persistencia.
     *
     * @return Objeto GroupEntity con los datos de este grupo.
     */
    @Override
    public GroupEntity toEntity() {
        return GroupEntity.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .creationDate(this.getCreationDate())
                .build();
    }
}
