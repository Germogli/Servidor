package com.germogli.backend.community.thread.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.thread.infrastructure.entity.ThreadReplyEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para una respuesta a un hilo.
 * Representa la información y lógica de negocio asociada a una respuesta.
 * Implementa Converter para estandarizar la conversión entre ThreadReplyEntity y ThreadReplyDomain.
 */
@Data
@SuperBuilder
public class ThreadReplyDomain extends BaseCommunityResource implements Converter<ThreadReplyDomain, ThreadReplyEntity> {
    private Integer id;
    private Integer threadId;
    private Integer userId;
    private String content;

    /**
     * Convierte una entidad ThreadReplyEntity en un objeto ThreadReplyDomain.
     * Método de instancia que delega en el método estático.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ThreadReplyDomain.
     */
    @Override
    public ThreadReplyDomain fromEntity(ThreadReplyEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una ThreadReplyEntity en un ThreadReplyDomain.
     * Permite usar la referencia de método: ThreadReplyDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ThreadReplyDomain con los datos de la entidad.
     */
    public static ThreadReplyDomain fromEntityStatic(ThreadReplyEntity entity) {
        return ThreadReplyDomain.builder()
                .id(entity.getId())
                .threadId(entity.getThreadId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto ThreadReplyDomain en una ThreadReplyEntity para persistencia.
     *
     * @return Objeto ThreadReplyEntity con los datos de esta respuesta.
     */
    @Override
    public ThreadReplyEntity toEntity() {
        return ThreadReplyEntity.builder()
                .id(this.id)
                .threadId(this.threadId)
                .userId(this.userId)
                .content(this.content)
                .creationDate(this.getCreationDate())
                .build();
    }
}
