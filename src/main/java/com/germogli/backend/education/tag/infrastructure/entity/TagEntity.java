package com.germogli.backend.education.tag.infrastructure.entity;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA que mapea la tabla "tags" en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EducationTagsEntity")
@Table(name = "tags")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer id;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String name;
}
