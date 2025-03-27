package com.germogli.backend.education.module.application.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateModuleRequestDTO {
    private String title;
    private String description;
    private Set<Integer> tagIds;
}
