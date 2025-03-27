package com.germogli.backend.education.module.application.dto;

import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.tag.application.dto.TagResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
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
    private Set<TagResponseDTO> tags;  // Usamos Set<TagResponseDTO> para las etiquetas

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
                .tags(domain.getTags().stream()
                        .map(tag -> TagResponseDTO.fromDomain(tag))  // Aquí mapeamos a objetos TagResponseDTO
                        .collect(Collectors.toSet()))  // Usamos Set para evitar duplicados
                .build();
    }

    /**
     * Convierte este DTO a un objeto ModuleDomain.
     */
    public ModuleDomain toDomain() {
        Set<TagDomain> tags = this.tags.stream()
                .map(tagDTO -> tagDTO.toDomain())  // Convertimos TagResponseDTO a TagDomain
                .collect(Collectors.toSet());

        return ModuleDomain.builder()
                .moduleId(this.moduleId)
                .title(this.title)
                .description(this.description)
                .creationDate(this.creationDate)
                .tags(tags)  // Asignamos los tags convertidos
                .build();
    }

    // Método para convertir una lista de dominios a lista de DTOs de respuesta
    public static List<ModuleResponseDTO> fromDomains(List<ModuleDomain> domains) {
        return domains.stream()
                .map(ModuleResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }
}
