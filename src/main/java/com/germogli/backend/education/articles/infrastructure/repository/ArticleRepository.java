package com.germogli.backend.education.articles.infrastructure.repository;

import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.repository.ArticleDomainRepository;
import com.germogli.backend.education.articles.infrastructure.entity.ArticleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar operaciones de artículos utilizando procedimientos almacenados.
 * Implementa la interfaz ArticleDomainRepository para realizar operaciones CRUD.
 */
@Repository("educationArticleRepository")
@RequiredArgsConstructor
public class ArticleRepository implements ArticleDomainRepository {

    // Instancia del EntityManager para interactuar con la base de datos
    private final EntityManager entityManager;

    /**
     * Crea un nuevo artículo en la base de datos utilizando un procedimiento almacenado.
     *
     * @param articleDomain El objeto ArticleDomain que representa el artículo a crear.
     * @return El objeto ArticleDomain creado con su ID generado.
     */
    @Override
    public ArticleDomain createArticle(ArticleDomain articleDomain) {
        // Crear la consulta del procedimiento almacenado para crear el artículo
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_article", ArticleEntity.class);

        // Registrar los parámetros de entrada y salida
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_article_url", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_article_id", Integer.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        query.setParameter("p_module_id", articleDomain.getModuleId().getModuleId());
        query.setParameter("p_title", articleDomain.getTitle());
        query.setParameter("p_article_url", articleDomain.getArticleUrl());

        // Ejecutar el procedimiento almacenado
        query.execute();

        // Obtener el ID generado del artículo y asignarlo al objeto de dominio
        Integer generatedId = (Integer) query.getOutputParameterValue("p_article_id");
        articleDomain.setArticleId(generatedId);

        return articleDomain;
    }

    /**
     * Obtiene los artículos que pertenecen a un módulo específico utilizando un procedimiento almacenado.
     *
     * @param moduleId El ID del módulo para filtrar los artículos.
     * @return Una lista de objetos ArticleDomain con los datos de los artículos.
     */
    @Override
    public List<ArticleDomain> getByArticlesByModuleId(Integer moduleId) {
        // Crear la consulta del SP y mapear el resultado a ArticleEntity
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_articles_by_module_id", ArticleEntity.class);

        // Registrar el parámetro de entrada para el SP
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);

        // Asignar el valor del parámetro
        query.setParameter("p_module_id", moduleId);

        // Ejecutar el SP
        query.execute();

        // Obtener la lista de resultados y mapearla a ArticleDomain
        List<ArticleEntity> resultList = query.getResultList();
        return resultList.stream()
                .map(ArticleDomain::fromEntityStatic)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un artículo por su ID en la base de datos utilizando un procedimiento almacenado.
     *
     * @param articleId El ID del artículo a obtener.
     * @return Un objeto ArticleDomain con los datos del artículo, o Optional.
     */
    @Override
    public Optional<ArticleDomain> getById(Integer articleId) {
        // Crear la consulta del procedimiento almacenado, mapeando el resultado a ArticleEntity
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_article_by_id", ArticleEntity.class);

        // Registrar el parámetro de entrada
        query.registerStoredProcedureParameter("p_article_id", Integer.class, ParameterMode.IN);

        // Asignar el valor del parámetro
        query.setParameter("p_article_id", articleId);

        // Ejecutar el SP
        query.execute();

        // Obtener la lista de resultados
        List<ArticleEntity> resultList = query.getResultList();

        // Si la lista está vacía, retornar Optional.empty(); de lo contrario, convertir el primer resultado
        if (resultList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(ArticleDomain.fromEntityStatic(resultList.get(0)));
        }
    }
}
