package com.germogli.backend.education.module.application.dto;

import com.germogli.backend.education.module.domain.model.ModuleDomain;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ModuleResponseDTO {
    private Integer moduleId;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private int tagCount;

    // Método para convertir un dominio a DTO de respuesta
    public static ModuleResponseDTO fromDomain(ModuleDomain domain) {
        return ModuleResponseDTO.builder()
                .moduleId(domain.getModuleId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .creationDate(domain.getCreationDate())
                .tagCount(domain.getTags().size())
                .build();
    }

    // Método para convertir una lista de dominios a lista de DTOs de respuesta
    public static List<ModuleResponseDTO> fromDomains(List<ModuleDomain> domains) {
        return domains.stream()
                .map(ModuleResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }
}