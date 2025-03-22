package com.germogli.backend.community.thread.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.thread.infrastructure.entity.ThreadEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para un hilo (thread) en la comunidad.
 * Representa la información y lógica de negocio asociada a un hilo.
 * Implementa Converter para estandarizar la conversión entre ThreadEntity y ThreadDomain.
 */
@Data
@SuperBuilder
public class ThreadDomain extends BaseCommunityResource implements Converter<ThreadDomain, ThreadEntity> {
    private Integer id;
    private Integer groupId;
    private Integer userId;
    private String title;
    private String content;

    /**
     * Convierte una entidad ThreadEntity en un objeto ThreadDomain.
     * Método de instancia que delega en el método estático.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ThreadDomain.
     */
    @Override
    public ThreadDomain fromEntity(ThreadEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una ThreadEntity en un ThreadDomain.
     * Permite usar la referencia de método: ThreadDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ThreadDomain con los datos de la entidad.
     */
    public static ThreadDomain fromEntityStatic(ThreadEntity entity) {
        return ThreadDomain.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto ThreadDomain en una ThreadEntity para persistencia.
     *
     * @return Objeto ThreadEntity con los datos de este hilo.
     */
    @Override
    public ThreadEntity toEntity() {
        return ThreadEntity.builder()
                .id(this.id)
                .groupId(this.groupId)
                .userId(this.userId)
                .title(this.title)
                .content(this.content)
                .creationDate(this.getCreationDate())
                .build();
    }
}
