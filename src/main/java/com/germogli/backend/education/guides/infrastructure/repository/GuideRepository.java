package com.germogli.backend.education.guides.infrastructure.repository;

import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.guides.domain.repository.GuideDomainRepository;
import com.germogli.backend.education.guides.infrastructure.entity.GuideEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
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

    /**
     * Obtiene todas las guías almacenadas en la base de datos utilizando un procedimiento almacenado.
     *
     * @return Una lista de objetos GuideDomain con los datos de las guías.
     */
    @Override
    public List<GuideDomain> getAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_guides", GuideEntity.class);
        query.execute();
        // Convertir el resultado de la consulta en una lista de objetos GuideDomain
        List<GuideEntity> resultList = query.getResultList();
        return resultList.stream().map(GuideDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Obtiene las guías de un módulo específico utilizando un procedimiento almacenado.
     *
     * @param moduleId El ID del módulo por el cual se filtran las guías.
     * @return Una lista de objetos GuideDomain con las guías correspondientes al módulo.
     */
    @Override
    public List<GuideDomain> getByModuleId(Integer moduleId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_guides_by_module_id", GuideEntity.class);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_module_id", moduleId);
        query.execute();
        // Convertir el resultado de la consulta en una lista de objetos GuideDomain
        List<GuideEntity> resultList = query.getResultList();
        return resultList.stream().map(GuideDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Obtiene una guía específica utilizando un procedimiento almacenado.
     *
     * @param guideId El ID de la guía a buscar.
     * @return Un objeto GuideDomain con los datos de la guía encontrada, o null si no se encontró.
     */
    @Override
    public Optional<GuideDomain> getById(Integer guideId) {
        // Crear la consulta de procedimiento almacenado, mapeando el resultado a GuideEntity
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_guide_by_id", GuideEntity.class);

        // Registrar el parámetro de entrada para el procedimiento almacenado
        query.registerStoredProcedureParameter("p_guide_id", Integer.class, ParameterMode.IN);

        // Asignar el valor del parámetro
        query.setParameter("p_guide_id", guideId);

        // Ejecutar el procedimiento almacenado
        query.execute();

        // Obtener la lista de resultados
        List<GuideEntity> resultList = query.getResultList();

        // Si no se encontró ninguna guía, retornar Optional.empty(), de lo contrario retornar el primer resultado
        if(resultList.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(GuideDomain.fromEntityStatic(resultList.get(0)));
        }
    }

    /**
     * Actualiza los datos de una guía en la base de datos utilizando un procedimiento almacenado.
     *
     * @param guideDomain La guía que se va a actualizar.
     * @return El objeto GuideDomain actualizado.
     */
    @Override
    public GuideDomain updateGuideInfo(GuideDomain guideDomain) {
        // Crear la consulta de procedimiento almacenado para actualizar la guía
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_guide_info");

        // Registrar los parámetros de entrada para el procedimiento almacenado
        query.registerStoredProcedureParameter("p_guide_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);

        // Establecer los valores de los parámetros de entrada
        query.setParameter("p_guide_id", guideDomain.getGuideId());
        query.setParameter("p_title", guideDomain.getTitle());
        query.setParameter("p_description", guideDomain.getDescription());
        query.setParameter("p_module_id", guideDomain.getModuleId() != null ? guideDomain.getModuleId().getModuleId() : null);

        // Ejecutar el procedimiento almacenado
        query.execute();

        // Retornar el objeto GuideDomain actualizado
        return guideDomain;
    }

}
