package com.germogli.backend.education.module.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DTO para la creación de un modulo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModuleResponseDTO {
    private String title;
    private String description;
    private Set<Integer> tagIds = new HashSet<>();
    private Map<Integer, String> tagNames = new HashMap<>();
}
