package com.germogli.backend.education.articles.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un articulo.
 * Contiene la información principal que se desea exponer al cliente.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponseDTO {
    private Integer articleId;
    private String title;
    private String articleUrl;
    private LocalDateTime creationDate;
    private Integer moduleId;
}
