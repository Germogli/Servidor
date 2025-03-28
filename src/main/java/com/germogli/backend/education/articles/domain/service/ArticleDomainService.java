package com.germogli.backend.education.articles.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.CustomForbiddenException;
import com.germogli.backend.education.articles.application.dto.CreateArticleRequestDTO;
import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.repository.ArticleDomainRepository;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArticleDomainService {

    // Dependencias inyectadas a través del constructor
    private final ArticleDomainRepository articleDomainRepository;
    private final ModuleDomainService moduleDomainService;
    private final EducationSharedService educationSharedService;

    /**
     * Crea un nuevo artículo educativo.
     *
     * @param dto Objeto CreateArticleRequestDTO con la información para crear el artículo.
     * @return El objeto ArticleDomain creado.
     */
    public ArticleDomain createArticle(CreateArticleRequestDTO dto) {
        // Obtener el usuario autenticado
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar si el usuario tiene el rol de "ADMINISTRADOR"
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para crear artículos.");
        }

     // Verificar que la URL del artículo no esté vacía
        if (dto.getArticleUrl() == null || dto.getArticleUrl().trim().isEmpty()) {
            throw new CustomForbiddenException("La URL del artículo no puede estar vacía");
        }

    // Verificar que el título del artículo no esté vacío
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new CustomForbiddenException("El título del artículo no puede estar vacío");
        }

        // Verificar que el módulo existe
        moduleDomainService.getModuleById(dto.getModuleId());

        // Crear el objeto ArticleDomain a partir del DTO
        ArticleDomain articleDomain = ArticleDomain.builder()
                .moduleId(com.germogli.backend.education.module.domain.model.ModuleDomain.builder()
                        .moduleId(dto.getModuleId())
                        .build())
                .title(dto.getTitle())
                .articleUrl(dto.getArticleUrl())
                .creationDate(LocalDateTime.now())
                .build();

        // Guardar el artículo en la base de datos utilizando el procedimiento almacenado
        return articleDomainRepository.createArticle(articleDomain);
    }
}
