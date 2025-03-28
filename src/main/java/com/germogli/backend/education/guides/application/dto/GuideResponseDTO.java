package com.germogli.backend.education.guides.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una guía.
 * Contiene la información principal que se desea exponer al cliente.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Esta anotación asegura que los campos nulos se incluyan
public class GuideResponseDTO {
    private Integer guideId;
    private String title;
    private String description;
    private String pdfUrl;
    private String pdfFileName;
    private LocalDateTime creationDate;
    private Integer moduleId; // Para conocer a qué módulo pertenece la guía

    /**
     * Convierte un objeto de dominio GuideDomain en GuideResponseDTO.
     *
     * @param domain Objeto de dominio a convertir.
     * @return DTO con los datos mapeados.
     */
    public static GuideResponseDTO fromDomain(GuideDomain domain) {

        // Logging adicional
        System.out.println("DTO Conversion - Domain PDF File Name: " + domain.getPdfFileName());

        return GuideResponseDTO.builder()
                .guideId(domain.getGuideId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .pdfUrl(domain.getPdfUrl())
                .pdfFileName(domain.getPdfFileName())
                .creationDate(domain.getCreationDate())
                .moduleId(domain.getModuleId() != null ? domain.getModuleId().getModuleId() : null)
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
                .pdfFileName(this.pdfFileName)
                .creationDate(this.creationDate)
                .moduleId(ModuleDomain.builder().moduleId(this.moduleId).build())
                .build();
    }
}
