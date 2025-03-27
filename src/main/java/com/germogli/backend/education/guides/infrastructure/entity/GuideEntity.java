package com.germogli.backend.education.guides.infrastructure.entity;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla "guides" en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EducationGuidesEntity")
@Table(name = "guides")
public class GuideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guide_id")
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, name = "pdf_url")
    private String pdfUrl;

    @Column(name = "publication_date", updatable = false, insertable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity moduleId;
}
