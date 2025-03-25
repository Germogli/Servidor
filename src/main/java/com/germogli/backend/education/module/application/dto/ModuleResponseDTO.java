package com.germogli.backend.education.module.application.dto;

import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class ModuleResponseDTO {
    private Integer moduleId;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private Set<String> tagNames = new HashSet<>();  // Cambiar a Set<String>

    /**
     * Método para convertir un objeto ModuleDomain a ModuleResponseDTO.
     *
     * @param domain el objeto de dominio a convertir.
     * @return el DTO de respuesta con los datos mapeados.
     */
    public static ModuleResponseDTO fromDomain(ModuleDomain domain) {
        return ModuleResponseDTO.builder()
                .moduleId(domain.getModuleId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .creationDate(domain.getCreationDate())
                .tagNames(domain.getTags().stream()
                        .map(TagDomain::getTagName)  // Aquí mapeamos a los nombres de las etiquetas
                        .collect(Collectors.toSet()))  // Usamos Set para evitar duplicados
                .build();
    }

    /**
     * Convierte este DTO a un objeto ModuleDomain.
     */
    public ModuleDomain toDomain() {
        // Convertir los nombres de etiquetas a objetos TagDomain
        Set<TagDomain> tags = this.tagNames.stream()
                .map(tagName -> TagDomain.builder()
                        .tagName(tagName)  // Aquí estamos creando objetos TagDomain usando el nombre
                        .build())
                .collect(Collectors.toSet());

        return ModuleDomain.builder()
                .moduleId(this.moduleId)
                .title(this.title)
                .description(this.description)
                .creationDate(this.creationDate)
                .tags(tags)
                .build();
    }

    // Método para convertir una lista de dominios a lista de DTOs de respuesta
    public static List<ModuleResponseDTO> fromDomains(List<ModuleDomain> domains) {
        return domains.stream()
                .map(ModuleResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }
}
