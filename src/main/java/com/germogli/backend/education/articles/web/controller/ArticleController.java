package com.germogli.backend.education.articles.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.articles.application.dto.ArticleResponseDTO;
import com.germogli.backend.education.articles.application.dto.CreateArticleRequestDTO;
import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.service.ArticleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleDomainService articleDomainService;

    /**
     * Método para crear un nuevo artículo educativo.
     * Recibe una solicitud HTTP POST con los datos necesarios para crear un artículo.
     *
     * @param dto Objeto CreateArticleRequestDTO con la información del artículo.
     * @return ResponseEntity con el resultado de la creación del artículo, incluyendo un mensaje y los detalles del artículo creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ArticleResponseDTO>> createArticle(
            @RequestBody CreateArticleRequestDTO dto
    ) {
        ArticleDomain createdArticle = articleDomainService.createArticle(dto);
        return ResponseEntity.ok(
                ApiResponseDTO.<ArticleResponseDTO>builder()
                        .message("Artículo creado correctamente")
                        .data(ArticleResponseDTO.fromDomain(createdArticle))
                        .build()
        );
    }
}