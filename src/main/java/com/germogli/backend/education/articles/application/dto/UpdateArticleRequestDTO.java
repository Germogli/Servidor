package com.germogli.backend.education.articles.application.dto;

import lombok.Data;

@Data
public class UpdateArticleRequestDTO {
    private Integer articleId;
    private Integer moduleId;
    private String title;
    private String articleUrl;
}
