package com.germogli.backend.education.videos.application.dto;

import com.germogli.backend.education.videos.domain.model.VideoDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un video.
 * Contiene la información principal que se desea exponer al cliente.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponseDTO {

    private Integer videoId;
    private String title;
    private String videoUrl;
    private LocalDateTime creationDate;
    private Integer moduleId;

    /**
     * Convierte un objeto de dominio VideoDomain en VideoResponseDTO.
     *
     * @param domain Objeto de dominio a convertir.
     * @return DTO con los datos mapeados.
     */
    public static VideoResponseDTO fromDomain(VideoDomain domain) {
        return VideoResponseDTO.builder()
                .videoId(domain.getVideoId())
                .title(domain.getTitle())
                .videoUrl(domain.getVideoUrl())
                .creationDate(domain.getCreationDate())
                .moduleId(domain.getModuleId() != null ? domain.getModuleId().getModuleId() : null)
                .build();
    }
}
