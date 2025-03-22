package com.germogli.backend.community.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class BaseEntity {
    @lombok.Builder.Default
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now();
}
