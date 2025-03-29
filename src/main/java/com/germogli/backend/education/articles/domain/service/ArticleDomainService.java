package com.germogli.backend.education.articles.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.CustomForbiddenException;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.articles.application.dto.ArticleResponseDTO;
import com.germogli.backend.education.articles.application.dto.CreateArticleRequestDTO;
import com.germogli.backend.education.articles.application.dto.UpdateArticleRequestDTO;
import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.repository.ArticleDomainRepository;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Obtiene los artículos asociados a un módulo específico.
     *
     * @param moduleId ID del módulo.
     * @return Lista de artículos (ArticleDomain) asociados al módulo.
     * @throws ResourceNotFoundException si no se encuentran artículos para el módulo.
     */
    public List<ArticleDomain> getArticlesByModuleId(Integer moduleId) {
        List<ArticleDomain> articles = articleDomainRepository.getByArticlesByModuleId(moduleId);
        if (articles.isEmpty()) {
            throw new ResourceNotFoundException("No hay artículos disponibles para este módulo.");
        }
        return articles;
    }

    /**
     * Obtiene un artículo educativo por su ID.
     *
     * @param id ID del artículo a buscar.
     * @return El objeto ArticleDomain correspondiente.
     * @throws ResourceNotFoundException si no se encuentra el artículo.
     */
    public ArticleDomain getArticleById(Integer id) {
        return articleDomainRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con id " + id));
    }

    /**
     * Actualiza un artículo educativo en la base de datos.
     *
     * @param articleId El ID del artículo a actualizar.
     * @param dto El objeto UpdateArticleRequestDTO con la nueva información del artículo.
     * @return El objeto ArticleDomain actualizado.
     * @throws ResourceNotFoundException si el artículo no se encuentra.
     */
    public ArticleDomain updateArticle(Integer articleId, UpdateArticleRequestDTO dto) {
        // Asignar el ID del artículo recibido al DTO
        dto.setArticleId(articleId);

        // Obtener el usuario autenticado
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar si el usuario tiene el rol de "ADMINISTRADOR"
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para actualizar artículos.");
        }

        // Verificar que el artículo existe; si no, lanzar una excepción
        ArticleDomain existingArticle = articleDomainRepository.getById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con id " + articleId));

        // Verificar que el módulo existe antes de actualizar, si se envía un módulo
        if (dto.getModuleId() != null) {
            moduleDomainService.getModuleById(dto.getModuleId());
        }

        // Crear el objeto ArticleDomain a partir del DTO, utilizando el ID recibido
        ArticleDomain articleDomain = ArticleDomain.builder()
                .articleId(articleId)                         // Asignar el ID del artículo
                .moduleId(com.germogli.backend.education.module.domain.model.ModuleDomain.builder()
                        .moduleId(dto.getModuleId())          // Módulo asociado
                        .build())
                .title(dto.getTitle())                        // Nuevo título
                .articleUrl(dto.getArticleUrl())              // Nueva URL del artículo
                .build();

        // Llamar al repositorio para realizar la actualización mediante el SP
        articleDomainRepository.updateArticleInfo(articleDomain);

        // Recuperar el artículo actualizado para obtener todos los campos, incluyendo creationDate
        ArticleDomain updatedArticle = articleDomainRepository.getById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Error al recuperar el artículo actualizado con id " + articleId));

        return updatedArticle;
    }


    /**
     * Convierte una lista de entidades de dominio a DTOs de respuesta.
     *
     * @param domains Lista de objetos GuideDomain que representan los artículos en la capa de dominio.
     * @return Lista de objetos ArticleResponseDTO con los datos formateados para la respuesta al cliente.
     */
    public List<ArticleResponseDTO> toResponseList(List<ArticleDomain> domains) {
        return domains.stream()
                .map(domain -> {
                    ArticleResponseDTO dto = new ArticleResponseDTO();
                    dto.setArticleId(domain.getArticleId());
                    dto.setTitle(domain.getTitle());
                    dto.setArticleUrl(domain.getArticleUrl());
                    dto.setCreationDate(domain.getCreationDate());
                    dto.setModuleId(domain.getModuleId().getModuleId());

                    // Devuelve el DTO convertido para el mapeo
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Elimina un artículo educativo de la base de datos.
     *
     * @param articleId El ID del artículo a eliminar.
     * @throws ResourceNotFoundException si el artículo no se encuentra.
     * @throws AccessDeniedException si el usuario no tiene permisos para eliminar artículos.
     */
    public void deleteArticle(Integer articleId) {
        // Obtener el usuario autenticado
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar si el usuario tiene el rol de "ADMINISTRADOR"
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para eliminar artículos.");
        }

        // Verificar que el artículo existe antes de intentar eliminarlo
        articleDomainRepository.getById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con id " + articleId));

        // Llamar al repositorio para eliminar el artículo
        articleDomainRepository.deleteById(articleId);
    }

}
