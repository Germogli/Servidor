package com.germogli.backend.community.reaction.domain.model;

import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.reaction.infrastructure.entity.ReactionEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;


/**
 * Modelo de dominio para una reacción.
 * Representa la información y la lógica de negocio asociada a una reacción en Community.
 * Implementa Converter para estandarizar la conversión entre ReactionEntity y ReactionDomain.
 */
@Data
@SuperBuilder
public class ReactionDomain implements Converter<ReactionDomain, ReactionEntity> {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String reactionType;
    private LocalDateTime reactionDate; // Renombrado para reflejar la columna reaction_date

    @Override
    public ReactionDomain fromEntity(ReactionEntity entity) {
        return fromEntityStatic(entity);
    }

    public static ReactionDomain fromEntityStatic(ReactionEntity entity) {
        return ReactionDomain.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .reactionType(entity.getReactionType())
                .reactionDate(entity.getReactionDate())
                .build();
    }

    @Override
    public ReactionEntity toEntity() {
        return ReactionEntity.builder()
                .id(this.id)
                .postId(this.postId)
                .userId(this.userId)
                .reactionType(this.reactionType)
                .reactionDate(this.reactionDate)
                .build();
    }
}