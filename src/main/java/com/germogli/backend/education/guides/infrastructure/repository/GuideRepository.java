package com.germogli.backend.education.guides.infrastructure.repository;

import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.guides.domain.repository.GuideDomainRepository;
import com.germogli.backend.education.guides.infrastructure.entity.GuideEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar operaciones de guías utilizando procedimientos almacenados.
 * Implementa la interfaz GuideDomainRepository para realizar operaciones CRUD.
 */
@Repository("educationGuideRepository")
@RequiredArgsConstructor
public class GuideRepository implements GuideDomainRepository {

    // Instancia del EntityManager para interactuar con la base de datos
    private final EntityManager entityManager;

    /**
     * Crea una nueva guía en la base de datos utilizando un procedimiento almacenado.
     *
     * @param guideDomain La guía que se va a crear.
     * @return La guía creada con su ID generado.
     */
    @Override
    public GuideDomain createGuide(GuideDomain guideDomain) {
        // Crear la consulta de procedimiento almacenado para crear la guía
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_guide");

        // Registrar los parámetros de entrada para el procedimiento almacenado
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_pdf_url", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("guide_id", Integer.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        query.setParameter("p_module_id", guideDomain.getModuleId().getModuleId());
        query.setParameter("p_title", guideDomain.getTitle());
        query.setParameter("p_description", guideDomain.getDescription());
        query.setParameter("p_pdf_url", guideDomain.getPdfUrl());

        // Ejecutar el procedimiento almacenado
        query.execute();

        // Obtener el ID generado de la guía y asignarlo al objeto guideDomain
        Integer generatedId = (Integer) query.getOutputParameterValue("guide_id");
        guideDomain.setGuideId(generatedId);

        // Retornar la guía con el ID generado
        return guideDomain;
    }

    @Override
    public List<GuideDomain> getAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_guides", GuideEntity.class);

        query.execute();

        List<GuideEntity> resultList = query.getResultList();
        return resultList.stream().map(GuideDomain::fromEntityStatic).collect(Collectors.toList());
    }



}
