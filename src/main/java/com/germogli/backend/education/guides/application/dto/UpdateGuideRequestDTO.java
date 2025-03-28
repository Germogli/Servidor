package com.germogli.backend.education.guides.application.dto;

import lombok.Data;

@Data
public class UpdateGuideRequestDTO {
    private Integer guideId;
    private Integer moduleId;
    private String title;
    private String description;
}
