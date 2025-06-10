package com.germogli.backend.education.tag.application.dto;

import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDTO {
    private Integer id;
    private String name;

    // Método estático para convertir de TagDomain a TagResponseDTO
    public static TagResponseDTO fromDomain(TagDomain domain) {
        return TagResponseDTO.builder()
                .id(domain.getTagId())
                .name(domain.getTagName())
                .build();
    }

    // Método para convertir de TagResponseDTO a TagDomain
    public TagDomain toDomain() {
        return TagDomain.builder()
                .tagId(this.id)
                .tagName(this.name)
                .build();
    }
}
