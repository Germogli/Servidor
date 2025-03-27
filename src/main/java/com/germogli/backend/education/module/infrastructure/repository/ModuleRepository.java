package com.germogli.backend.education.module.infrastructure.repository;

import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.repository.ModuleDomainRepository;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar operaciones de módulos utilizando procedimientos almacenados.
 * Implementa la interfaz ModuleDomainRepository para realizar operaciones CRUD.
 */
@Repository("educationModuleRepository")
@RequiredArgsConstructor
public class ModuleRepository implements ModuleDomainRepository {
    private final EntityManager entityManager;

    /**
     * Crea un nuevo módulo con sus etiquetas asociadas.
     *
     * @param moduleDomain El módulo de dominio a crear.
     * @return El módulo creado con su ID generado.
     */
    @Override
    public ModuleDomain createModuleWithTags(ModuleDomain moduleDomain) {
        // Convertir el conjunto de etiquetas a una cadena de IDs separados por comas
        String tagIds = moduleDomain.getTags()
                .stream()
                .map(TagDomain::getTagId)  // Cambiar de getTagName a getTagId
                .map(String::valueOf)      // Convertir los IDs a String
                .collect(Collectors.joining(","));

        // Crear consulta de procedimiento almacenado para crear módulo con etiquetas
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_module_with_tags");
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_tags", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("module_id", Integer.class, ParameterMode.OUT);

        // Establecer parámetros del procedimiento
        query.setParameter("p_title", moduleDomain.getTitle());
        query.setParameter("p_description", moduleDomain.getDescription());
        query.setParameter("p_tags", tagIds);  // Pasar los IDs en lugar de los nombres
        query.execute();

        // Obtener el ID generado y establecerlo en el módulo
        Integer generatedId = (Integer) query.getOutputParameterValue("module_id");
        moduleDomain.setModuleId(generatedId);
        return moduleDomain;
    }

    /**
     * Recupera todos los módulos con sus etiquetas asociadas.
     *
     * @return Lista de módulos.
     */
    @Override
    public List<ModuleDomain> getAll() {
        // Ejecutar procedimiento almacenado para obtener módulos con etiquetas
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_modules_with_tags", ModuleEntity.class);
        query.execute();

        // Convertir las entidades recuperadas a objetos de dominio
        List<ModuleEntity> resultList = query.getResultList();
        return resultList.stream().map(ModuleDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Actualiza un módulo existente y sus etiquetas asociadas.
     *
     * @param moduleDomain El módulo de dominio a actualizar.
     * @return El módulo actualizado.
     */
    @Override
    public ModuleDomain updateModuleWithTags(ModuleDomain moduleDomain) {
        // Convertir el conjunto de etiquetas a una cadena de IDs separados por comas
        String tagIds = moduleDomain.getTags()
                .stream()
                .map(TagDomain::getTagId)  // Obtener los IDs de las etiquetas
                .map(String::valueOf)      // Convertirlos a String
                .collect(Collectors.joining(",")); // Unirlos con comas

        // Crear consulta de procedimiento almacenado para actualizar módulo con etiquetas
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_module_with_tags");

        // Registrar parámetros del procedimiento almacenado
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_tag_ids", String.class, ParameterMode.IN);

        // Asignar valores a los parámetros
        query.setParameter("p_module_id", moduleDomain.getModuleId());
        query.setParameter("p_title", moduleDomain.getTitle());
        query.setParameter("p_description", moduleDomain.getDescription());
        query.setParameter("p_tag_ids", tagIds);  // Pasar los IDs en lugar de nombres

        // Ejecutar el procedimiento almacenado
        query.execute();
        return moduleDomain; // Retornar el módulo actualizado
    }

    /**
     * Busca un módulo por su identificador único.
     *
     * @param moduleId El ID del módulo a buscar.
     * @return Un Optional que contiene el módulo si se encuentra, o vacío si no existe.
     */
    @Override
    public Optional<ModuleDomain> getById(Integer moduleId) {
        // Crear consulta de procedimiento almacenado para obtener módulo por ID
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_module_by_id", ModuleEntity.class);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_module_id", moduleId);
        query.execute();

        // Obtener los resultados
        List<ModuleEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }

        // Convertir la entidad al dominio y devolver
        return Optional.of(ModuleDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Filtra módulos por una lista de etiquetas.
     *
     * @param tagIds Lista de IDs de etiquetas para filtrar.
     * @return Lista de módulos que coinciden con las etiquetas especificadas.
     */
    @Override
    public List<ModuleDomain> filterModulesByTags(List<Integer> tagIds) {
        // Convertir la lista de IDs de etiquetas a una cadena separada por comas
        String tagIdsStr = tagIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Crear la consulta al procedimiento almacenado
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_search_modules_by_tags");
        query.registerStoredProcedureParameter("p_tag_ids", String.class, ParameterMode.IN);
        query.setParameter("p_tag_ids", tagIdsStr);
        query.execute();

        // Obtener resultados y procesar
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();

        // Agrupar los resultados por módulo
        Map<Integer, ModuleDomain> moduleMap = new HashMap<>();
        for (Object[] result : resultList) {
            Integer moduleId = (Integer) result[0];

            // Si el módulo aún no está en el mapa, crearlo
            if (!moduleMap.containsKey(moduleId)) {
                LocalDateTime creationDate = result[3] instanceof Timestamp
                        ? ((Timestamp) result[3]).toLocalDateTime()
                        : null;
                ModuleDomain module = ModuleDomain.builder()
                        .moduleId(moduleId)
                        .title((String) result[1])
                        .description((String) result[2])
                        .creationDate(creationDate)
                        .tags(new HashSet<>())
                        .build();
                moduleMap.put(moduleId, module);
            }

            // Agregar la etiqueta al módulo
            TagDomain tag = TagDomain.builder()
                    .tagId((Integer) result[4])
                    .tagName((String) result[5])
                    .build();
            moduleMap.get(moduleId).getTags().add(tag);
        }

        return new ArrayList<>(moduleMap.values());
    }

    /**
     * Elimina un módulo por su identificador único.
     *
     * @param moduleId El ID del módulo a eliminar.
     */
    @Override
    public void deleteModule(Integer moduleId) {
        // Crear consulta de procedimiento almacenado para eliminar módulo
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_module");
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_module_id", moduleId);
        query.execute();
    }
}