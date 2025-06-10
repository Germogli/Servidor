package com.germogli.backend.education.articles.application.dto;

import com.germogli.backend.education.articles.domain.model.ArticleDomain;
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

    /**
     * Convierte un objeto de dominio ArticleDomain en ArticleResponseDTO.
     *
     * @param domain Objeto de dominio a convertir.
     * @return DTO con los datos mapeados.
     */
    public static ArticleResponseDTO fromDomain(ArticleDomain domain) {
        return ArticleResponseDTO.builder()
                .articleId(domain.getArticleId())
                .title(domain.getTitle())
                .articleUrl(domain.getArticleUrl())
                .creationDate(domain.getCreationDate())
                .moduleId(domain.getModuleId() != null ? domain.getModuleId().getModuleId() : null)
                .build();
    }
}
