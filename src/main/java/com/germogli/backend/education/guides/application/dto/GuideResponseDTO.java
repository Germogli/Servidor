package com.germogli.backend.education.guides.application.dto;

import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una guía.
 * Contiene la información principal que se desea exponer al cliente.
 */
@Data
@Builder
public class GuideResponseDTO {
    private Integer guideId;
    private String title;
    private String description;
    private String pdfUrl;
    private LocalDateTime creationDate;
    private Integer moduleId; // Para conocer a qué módulo pertenece la guía

    /**
     * Convierte un objeto de dominio GuideDomain en GuideResponseDTO.
     *
     * @param domain Objeto de dominio a convertir.
     * @return DTO con los datos mapeados.
     */
    public static GuideResponseDTO fromDomain(GuideDomain domain) {
        return GuideResponseDTO.builder()
                .guideId(domain.getGuideId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .pdfUrl(domain.getPdfUrl())
                .creationDate(domain.getCreationDate())
                .moduleId(domain.getModule() != null ? domain.getModule().getModuleId() : null)
                .build();
    }

    /**
     * Convierte este DTO en un objeto de dominio GuideDomain.
     *
     * @return Objeto GuideDomain con los datos del DTO.
     */
    public GuideDomain toDomain() {
        return GuideDomain.builder()
                .guideId(this.guideId)
                .title(this.title)
                .description(this.description)
                .pdfUrl(this.pdfUrl)
                .creationDate(this.creationDate)
                .module(ModuleDomain.builder().moduleId(this.moduleId).build())
                .build();
    }
}
