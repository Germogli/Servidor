package com.germogli.backend.education.videos.domain.model;

import com.germogli.backend.education.domain.model.Converter;
import com.germogli.backend.education.videos.infrastructure.entity.VideoEntity;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para un video educativo.
 * Representa la lógica de negocio y se utiliza en el servicio.
 * Implementa la interfaz Converter para facilitar la conversión entre VideoDomain y VideoEntity.
 */
@Data
@Builder
public class VideoDomain implements Converter<VideoDomain, VideoEntity> {

    private Integer videoId;
    private String title;
    private String videoUrl;
    private LocalDateTime creationDate;
    private ModuleDomain moduleId;

    /**
     * Convierte una entidad VideoEntity en un objeto de dominio VideoDomain.
     *
     * @param entity Entidad VideoEntity a convertir.
     * @return Instancia de VideoDomain con los datos mapeados.
     */
    @Override
    public VideoDomain fromEntity(VideoEntity entity) {
        return VideoDomain.builder()
                .videoId(entity.getId())
                .title(entity.getTitle())
                .videoUrl(entity.getVideoUrl())
                .creationDate(entity.getCreationDate())
                .moduleId(ModuleDomain.builder()
                        .moduleId(entity.getModuleId().getId())
                        .build())
                .build();
    }

    /**
     * Método estático que convierte una entidad VideoEntity en un objeto de dominio VideoDomain.
     *
     * @param entity La entidad VideoEntity a convertir.
     * @return Instancia de VideoDomain con los datos mapeados.
     */
    public static VideoDomain fromEntityStatic(VideoEntity entity) {
        return VideoDomain.builder()
                .videoId(entity.getId())
                .title(entity.getTitle())
                .videoUrl(entity.getVideoUrl())
                .creationDate(entity.getCreationDate())
                .moduleId(ModuleDomain.builder()
                        .moduleId(entity.getModuleId().getId())
                        .build())
                .build();
    }

    /**
     * Convierte este objeto de dominio en una entidad VideoEntity.
     *
     * @return La entidad VideoEntity resultante.
     */
    @Override
    public VideoEntity toEntity() {
        return VideoEntity.builder()
                .id(this.videoId)
                .title(this.title)
                .videoUrl(this.videoUrl)
                .creationDate(this.creationDate)
                .moduleId(ModuleEntity.builder()
                        .id(this.moduleId.getModuleId())
                        .build())
                .build();
    }
}
