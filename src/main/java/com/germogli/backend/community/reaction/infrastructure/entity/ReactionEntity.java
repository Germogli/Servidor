package com.germogli.backend.community.reaction.infrastructure.entity;

import com.germogli.backend.community.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reacción.
 * Mapea la tabla reactions.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CommunityReactionEntity")
@Table(name = "reactions")
public class ReactionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Integer id;

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "reaction_type", nullable = false, length = 50)
    private String reactionType;
}
