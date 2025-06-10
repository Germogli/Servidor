package com.germogli.backend.education.videos.infrastructure.entity;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla "videos" en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EducationVideosEntity")
@Table(name = "videos")
public class VideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "video_url")
    private String videoUrl;

    @Column(name = "publication_date", updatable = false, insertable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity moduleId;
}
