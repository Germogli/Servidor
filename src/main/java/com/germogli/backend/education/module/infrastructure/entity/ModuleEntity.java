package com.germogli.backend.education.module.infrastructure.entity;

import com.germogli.backend.education.tag.infrastructure.entity.TagEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA que mapea la tabla "modules" en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EducationModulesEntity")
@Table(name = "modules")
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "creation_date", updatable = false, insertable = false)
    private LocalDateTime creationDate;

    // Relación Many-to-Many con Tag usando la tabla intermedia module_tags
    @ManyToMany
    @JoinTable(
            name = "module_tags",
            joinColumns = @JoinColumn(name = "module_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();
}
