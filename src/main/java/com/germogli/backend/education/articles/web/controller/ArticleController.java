package com.germogli.backend.education.articles.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.articles.application.dto.ArticleResponseDTO;
import com.germogli.backend.education.articles.application.dto.CreateArticleRequestDTO;
import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.service.ArticleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Obtiene los artículos correspondientes a un módulo específico.
     *
     * @param moduleId ID del módulo para filtrar los artículos.
     * @return ResponseEntity con la lista de artículos en un ApiResponseDTO.
     */
    @GetMapping("/getByModuleId/{moduleId}")
    public ResponseEntity<ApiResponseDTO<List<ArticleResponseDTO>>> getArticlesByModuleId(@PathVariable Integer moduleId) {
        // Obtener la lista de artículos a través del servicio
        List<ArticleResponseDTO> articles = articleDomainService.toResponseList(articleDomainService.getArticlesByModuleId(moduleId));
        return ResponseEntity.ok(
                ApiResponseDTO.<List<ArticleResponseDTO>>builder()
                        .message("Artículos recuperados correctamente para el módulo con id " + moduleId)
                        .data(articles)
                        .build()
        );
    }

    /**
     * Obtiene un artículo específico por su ID.
     *
     * @param id ID del artículo a buscar.
     * @return ResponseEntity con el artículo en un ApiResponseDTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ArticleResponseDTO>> getArticleById(@PathVariable Integer id) {
        ArticleDomain articleDomain = articleDomainService.getArticleById(id);
        ArticleResponseDTO articleDTO = ArticleResponseDTO.fromDomain(articleDomain);
        return ResponseEntity.ok(
                ApiResponseDTO.<ArticleResponseDTO>builder()
                        .message("Artículo recuperado correctamente")
                        .data(articleDTO)
                        .build()
        );
    }

}