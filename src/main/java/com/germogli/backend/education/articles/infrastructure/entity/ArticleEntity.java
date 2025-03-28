package com.germogli.backend.education.articles.infrastructure.entity;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla "articles" en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EducationArticlesEntity")
@Table(name = "articles")
public class ArticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "article_url")
    private String articleUrl;

    @Column(name = "publication_date", updatable = false, insertable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity moduleId;
}
