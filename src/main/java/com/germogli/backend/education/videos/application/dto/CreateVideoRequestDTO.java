package com.germogli.backend.education.videos.application.dto;

import lombok.Data;

@Data
public class CreateVideoRequestDTO {
    private Integer moduleId;
    private String title;
    private String videoUrl;
}
