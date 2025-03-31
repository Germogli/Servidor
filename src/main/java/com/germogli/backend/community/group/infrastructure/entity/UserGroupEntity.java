package com.germogli.backend.community.group.infrastructure.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

/**
 * Entidad JPA que representa la tabla intermedia user_groups.
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_groups")
public class UserGroupEntity {

    @EmbeddedId
    private UserGroupId id;

    @Column(name = "join_date", nullable = false)
    @lombok.Builder.Default
    private LocalDateTime joinDate = LocalDateTime.now();
}
