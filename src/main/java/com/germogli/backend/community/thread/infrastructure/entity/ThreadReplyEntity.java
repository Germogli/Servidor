package com.germogli.backend.community.thread.infrastructure.entity;

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

/**
 * Entidad JPA que representa una respuesta a un hilo (thread reply).
 * Mapea la tabla "thread_replies" y extiende BaseEntity para heredar el campo creationDate.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity(name = "CommunityThreadReplyEntity")
@Table(name = "thread_replies")
public class ThreadReplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Integer id;

    @Column(name = "thread_id", nullable = false)
    private Integer threadId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
