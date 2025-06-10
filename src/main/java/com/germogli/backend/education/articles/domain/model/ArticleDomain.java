package com.germogli.backend.education.articles.domain.model;

import com.germogli.backend.education.domain.model.Converter;
import com.germogli.backend.education.articles.infrastructure.entity.ArticleEntity;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para un artículo educativo.
 * Representa la lógica de negocio y se utiliza en el servicio.
 * Implementa la interfaz Converter para facilitar la conversión entre ArticleDomain y ArticleEntity.
 */
@Data
@Builder
public class ArticleDomain implements Converter<ArticleDomain, ArticleEntity> {
    private Integer articleId;
    private String title;
    private String articleUrl;
    private LocalDateTime creationDate;
    private ModuleDomain moduleId;

    /**
     * Convierte una entidad ArticleEntity en un objeto de dominio ArticleDomain.
     *
     * @param entity Entidad ArticleEntity a convertir.
     * @return Instancia de ArticleDomain con los datos mapeados.
     */
    @Override
    public ArticleDomain fromEntity(ArticleEntity entity) {
        return ArticleDomain.builder()
                .articleId(entity.getId())
                .title(entity.getTitle())
                .articleUrl(entity.getArticleUrl())
                .creationDate(entity.getCreationDate())
                .moduleId(ModuleDomain.builder()
                        .moduleId(entity.getModuleId().getId())
                        .build())
                .build();
    }

    /**
     * Método estático que convierte una entidad ArticleEntity en un objeto de dominio ArticleDomain.
     *
     * @param entity La entidad ArticleEntity a convertir.
     * @return Instancia de ArticleDomain con los datos mapeados.
     */
    public static ArticleDomain fromEntityStatic(ArticleEntity entity) {
        return ArticleDomain.builder()
                .articleId(entity.getId())
                .title(entity.getTitle())
                .articleUrl(entity.getArticleUrl())
                .creationDate(entity.getCreationDate())
                .moduleId(ModuleDomain.builder()
                        .moduleId(entity.getModuleId().getId())
                        .build())
                .build();
    }

    /**
     * Convierte este objeto de dominio en una entidad ArticleEntity.
     *
     * @return La entidad ArticleEntity resultante.
     */
    @Override
    public ArticleEntity toEntity() {
        return ArticleEntity.builder()
                .id(this.articleId)
                .title(this.title)
                .articleUrl(this.articleUrl)
                .creationDate(this.creationDate)
                .moduleId(ModuleEntity.builder()
                        .id(this.moduleId.getModuleId())
                        .build())
                .build();
    }
}
