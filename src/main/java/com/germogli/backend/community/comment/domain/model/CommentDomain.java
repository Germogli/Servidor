package com.germogli.backend.community.comment.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.comment.infrastructure.entity.CommentEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para un comentario.
 * Representa la lógica de negocio y los datos asociados a un comentario en Community.
 * Implementa Converter para estandarizar la conversión entre CommentEntity y CommentDomain.
 */
@Data
@SuperBuilder
public class CommentDomain extends BaseCommunityResource implements Converter<CommentDomain, CommentEntity> {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String content;
    private Integer threadId;  // Si el comentario está asociado a un hilo
    private Integer groupId;   // Si el comentario está asociado a un grupo

    /**
     * Método de instancia para convertir una entidad en un objeto CommentDomain.
     * Implementa la interfaz Converter.
     *
     * @param entity Entidad a convertir.
     * @return Instancia de CommentDomain con los datos de la entidad.
     */
    @Override
    public CommentDomain fromEntity(CommentEntity entity) {
        return CommentDomain.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .threadId(entity.getThreadId())
                .groupId(entity.getGroupId())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Método estático para convertir una entidad en un objeto CommentDomain.
     * Se utiliza para referencias de método estático, por ejemplo, en streams.
     *
     * @param entity Entidad a convertir.
     * @return Instancia de CommentDomain.
     */
    public static CommentDomain fromEntityStatic(CommentEntity entity) {
        return CommentDomain.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .threadId(entity.getThreadId())
                .groupId(entity.getGroupId())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto CommentDomain en una entidad CommentEntity para persistencia.
     *
     * @return Instancia de CommentEntity.
     */
    @Override
    public CommentEntity toEntity() {
        return CommentEntity.builder()
                .id(this.id)
                .postId(this.postId)
                .userId(this.userId)
                .content(this.content)
                .threadId(this.threadId)
                .groupId(this.groupId)
                .creationDate(this.getCreationDate())
                .build();
    }
}
