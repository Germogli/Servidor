package com.germogli.backend.community.comment.infrastructure.entity;

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
 * Entidad JPA que representa un comentario.
 * Hereda el campo creationDate de BaseEntity.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CommunityCommentEntity")
@Table(name = "comments")
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer id;

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Fecha del comentario. Se utiliza el campo creationDate heredado en BaseEntity
     * o se mapea a una columna específica si se requiere.
     */
    @Column(name = "comment_date", nullable = false)
    private LocalDateTime commentDate;

    @Column(name = "thread_id")
    private Integer threadId;

    @Column(name = "group_id")
    private Integer groupId;
}
