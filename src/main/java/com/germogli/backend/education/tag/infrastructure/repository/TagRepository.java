package com.germogli.backend.education.tag.infrastructure.repository;

import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository("educationTagRepository")
@RequiredArgsConstructor
public class TagRepository implements TagDomainRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Integer getOrCreateTag(TagDomain tag) {
        try {
            // Crear el StoredProcedureQuery para sp_get_or_create_tag
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_or_create_tag");

            // Registrar los parámetros del procedimiento: uno de entrada y otro de salida
            query.registerStoredProcedureParameter("p_tag_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_tag_id", Integer.class, ParameterMode.OUT);

            // Asignar el valor del parámetro de entrada
            query.setParameter("p_tag_name", tag.getTagName());

            // Ejecutar el procedimiento almacenado
            query.execute();

            // Obtener el valor del parámetro de salida (el ID de la etiqueta)
            return (Integer) query.getOutputParameterValue("p_tag_id");
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al obtener o crear la etiqueta: " + tag.getTagName());
        }
    }
}
