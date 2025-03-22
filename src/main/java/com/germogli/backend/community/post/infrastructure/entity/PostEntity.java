package com.germogli.backend.community.post.infrastructure.entity;

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
 * Entidad JPA que representa una publicación.
 * Mapea la tabla posts.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CommunityPostEntity")
@Table(name = "posts")
public class PostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "post_type", nullable = false, length = 50)
    private String postType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "multimedia_content", length = 255)
    private String multimediaContent;

    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "thread_id")
    private Integer threadId;
}
