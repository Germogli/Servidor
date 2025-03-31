package com.germogli.backend.community.message.domain.model;

import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.message.infrastructure.entity.MessageEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para un mensaje.
 * Representa la lógica de negocio y los datos asociados a un mensaje en Community.
 * Implementa Converter para estandarizar la conversión entre MessageEntity y MessageDomain.
 */
@Data
@SuperBuilder
public class MessageDomain implements Converter<MessageDomain, MessageEntity> {
    private Integer id;
    private Integer postId;    // Si el mensaje está asociado a un post
    private Integer userId;
    private String content;
    private Integer threadId;  // Si el mensaje está asociado a un hilo
    private Integer groupId;   // Si el mensaje está asociado a un grupo
    private LocalDateTime creationDate;

    /**
     * Convierte una entidad MessageEntity en un objeto MessageDomain.
     *
     * @param entity Entidad a convertir.
     * @return Instancia de MessageDomain con los datos de la entidad.
     */
    @Override
    public MessageDomain fromEntity(MessageEntity entity) {
        return MessageDomain.builder()
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
     * Método estático para convertir una entidad en un objeto MessageDomain.
     *
     * @param entity Entidad a convertir.
     * @return Instancia de MessageDomain.
     */
    public static MessageDomain fromEntityStatic(MessageEntity entity) {
        return MessageDomain.builder()
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
     * Convierte este objeto MessageDomain en una entidad MessageEntity para persistencia.
     *
     * @return Instancia de MessageEntity.
     */
    @Override
    public MessageEntity toEntity() {
        return MessageEntity.builder()
                .id(this.id)
                .postId(this.postId)
                .userId(this.userId)
                .content(this.content)
                .threadId(this.threadId)
                .groupId(this.groupId)
                .creationDate(this.creationDate)
                .build();
    }
}
