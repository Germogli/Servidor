package com.germogli.backend.community.reaction.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.reaction.infrastructure.entity.ReactionEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para una reacción.
 * Representa la información y la lógica de negocio asociada a una reacción en Community.
 * Implementa la interfaz Converter para estandarizar la conversión entre ReactionEntity y ReactionDomain.
 */
@Data
@SuperBuilder
public class ReactionDomain extends BaseCommunityResource implements Converter<ReactionDomain, ReactionEntity> {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String reactionType;

    /**
     * Método de instancia requerido por la interfaz Converter.
     * Se delega en el método estático para permitir el uso de referencias de método.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ReactionDomain con los datos de la entidad.
     */
    @Override
    public ReactionDomain fromEntity(ReactionEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una ReactionEntity en un ReactionDomain.
     * Permite utilizar la referencia de método: ReactionDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto ReactionDomain con los datos de la entidad.
     */
    public static ReactionDomain fromEntityStatic(ReactionEntity entity) {
        return ReactionDomain.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .reactionType(entity.getReactionType())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto ReactionDomain en una ReactionEntity para persistencia.
     *
     * @return Objeto ReactionEntity con los datos de esta reacción.
     */
    @Override
    public ReactionEntity toEntity() {
        return ReactionEntity.builder()
                .id(this.id)
                .postId(this.postId)
                .userId(this.userId)
                .reactionType(this.reactionType)
                .creationDate(this.getCreationDate())
                .build();
    }
}
