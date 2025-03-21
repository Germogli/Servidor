package com.germogli.backend.education.tag.infrastructure.repository;

import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import com.germogli.backend.education.tag.infrastructure.entity.TagEntity;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("educationTagRepository")
@RequiredArgsConstructor
public class TagRepository implements TagDomainRepository {

    private final EntityManager entityManager;

    @Override
    public TagDomain save(String tagName) {
        try {
            // Crear el StoredProcedureQuery para el procedimiento almacenado
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_tag");

            // Registrar parámetros de entrada y salida
            query.registerStoredProcedureParameter("p_tag_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_tag_id", Integer.class, ParameterMode.OUT);

            // Asignar los valores de los parámetros
            query.setParameter("p_tag_name", tagName);

            // Ejecutar el procedimiento almacenado
            query.execute();

            // Obtener el ID generado por el SP
            Integer tagId = (Integer) query.getOutputParameterValue("p_tag_id");

            // Crear el objeto TagDomain usando el builder
            TagDomain tagDomain = TagDomain.builder()
                    .tagId(tagId) // ID generado
                    .tagName(tagName) // Nombre de la etiqueta
                    .build();

            return tagDomain;
        } catch (NoResultException e) {
            // Manejo de excepción si no se encuentra el resultado esperado
            throw new RuntimeException("No se encontró el resultado al crear la etiqueta: " + tagName, e);
        } catch (Exception e) {
            // Manejo genérico de excepciones utilizando ExceptionHandlerUtil
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al crear la etiqueta: " + tagName);
        }
    }

    @Override
    public TagDomain getByName(String tagName) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_tag_by_name", TagEntity.class);
            query.registerStoredProcedureParameter("p_tag_name", String.class, ParameterMode.IN);
            query.setParameter("p_tag_name", tagName);
            query.execute();

            // Se espera que el SP devuelva una única fila
            List<TagEntity> resultList = query.getResultList();
            if (resultList.isEmpty()) {
                return null;
            }

            // Tomamos el primer (y único) resultado y lo convertimos a TagDomain
            TagEntity entity = resultList.get(0);
            return TagDomain.fromEntity(entity);
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al buscar la etiqueta: " + tagName);
        }
    }

    @Override
    public void deleteById(Integer tagId) {
        try {
            // Llamar al procedimiento almacenado para eliminar la etiqueta por ID
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_tag_by_id");
            query.registerStoredProcedureParameter("p_tag_id", Integer.class, ParameterMode.IN);
            query.setParameter("p_tag_id", tagId);
            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al eliminar la etiqueta con ID: " + tagId);
        }
    }

    @Override
    public void updateTagName(TagDomain tag) {
        try {
            // Llamar al procedimiento almacenado para actualizar el nombre de la etiqueta
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_tag_name_by_id");

            // Registrar parámetros para el SP
            query.registerStoredProcedureParameter("p_tag_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_new_tag_name", String.class, ParameterMode.IN);

            // Asignar los valores de los parámetros
            query.setParameter("p_tag_id", tag.getTagId());
            query.setParameter("p_new_tag_name", tag.getTagName());

            // Ejecutar el procedimiento almacenado
            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar el nombre de la etiqueta con ID: " + tag.getTagId());
        }
    }
}
