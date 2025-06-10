package com.germogli.backend.education.videos.application.dto;

import lombok.Data;

@Data
public class UpdateVideoRequestDTO {
    private Integer videoId;
    private Integer moduleId;
    private String title;
    private String videoUrl;
}
