package com.germogli.backend.education.articles.infrastructure.repository;

import com.germogli.backend.education.articles.domain.model.ArticleDomain;
import com.germogli.backend.education.articles.domain.repository.ArticleDomainRepository;
import com.germogli.backend.education.articles.infrastructure.entity.ArticleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
