package com.germogli.backend.community.group.domain.model;

import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.group.infrastructure.entity.GroupEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para un grupo de la comunidad.
 * Representa la lógica y los datos asociados a un grupo.
 * Implementa la interfaz Converter para estandarizar la conversión entre
 * la entidad (GroupEntity) y el modelo de dominio.
 */
@Data
@SuperBuilder
public class GroupDomain implements Converter<GroupDomain, GroupEntity> {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime creationDate;

    /**
     * Convierte una entidad GroupEntity en un objeto GroupDomain.
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
                .creationDate(this.creationDate)
                .build();
    }
}
