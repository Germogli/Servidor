package com.germogli.backend.community.post.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para una publicación.
 * Representa la información y la lógica de negocio asociada a una publicación en la comunidad.
 * Implementa Converter para estandarizar la conversión entre PostEntity y PostDomain.
 */
@Data
@SuperBuilder
public class PostDomain extends BaseCommunityResource implements Converter<PostDomain, PostEntity> {
    private Integer id;
    private Integer userId;
    private String postType;
    private String content;
    private String multimediaContent;
    private LocalDateTime postDate;
    private Integer groupId;
    private Integer threadId;

    /**
     * Método de instancia requerido por la interfaz Converter.
     * Delegamos en el método estático para permitir el uso de referencias.
     *
     * @param entity Entidad a convertir.
     * @return Objeto PostDomain.
     */
    @Override
    public PostDomain fromEntity(PostEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad PostEntity en un objeto PostDomain.
     * Permite usar la referencia de método: PostDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto PostDomain con los datos de la entidad.
     */
    public static PostDomain fromEntityStatic(PostEntity entity) {
        return PostDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .postType(entity.getPostType())
                .content(entity.getContent())
                .multimediaContent(entity.getMultimediaContent())
                .postDate(entity.getPostDate())
                .groupId(entity.getGroupId())
                .threadId(entity.getThreadId())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto PostDomain en una entidad PostEntity para persistencia.
     *
     * @return Objeto PostEntity con los datos de este post.
     */
    @Override
    public PostEntity toEntity() {
        return PostEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .postType(this.postType)
                .content(this.content)
                .multimediaContent(this.multimediaContent)
                .postDate(this.postDate)
                .groupId(this.groupId)
                .threadId(this.threadId)
                .creationDate(this.getCreationDate())
                .build();
    }
}
