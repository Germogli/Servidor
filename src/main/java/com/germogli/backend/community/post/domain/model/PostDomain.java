package com.germogli.backend.community.post.domain.model;

import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio para la publicación.
 * Este modelo representa la lógica de negocio y se utiliza en el servicio.
 */
@Data
@Builder
public class PostDomain {
    private Integer id;
    private Integer userId;
    private String postType;
    private String content;
    private String multimediaContent;
    private LocalDateTime postDate;
    private Integer groupId;
    private Integer threadId;

    // Conversión de entidad a dominio
    public static PostDomain fromEntity(PostEntity entity) {
        return PostDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .postType(entity.getPostType())
                .content(entity.getContent())
                .multimediaContent(entity.getMultimediaContent())
                .postDate(entity.getPostDate())
                .groupId(entity.getGroupId())
                .threadId(entity.getThreadId())
                .build();
    }

    // Conversión de dominio a entidad
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
                .build();
    }
}
