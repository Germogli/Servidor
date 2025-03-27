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

@Repository("educationModuleRepository")
@RequiredArgsConstructor
public class ModuleRepository implements ModuleDomainRepository {

    private final EntityManager entityManager;
    private final TagDomainRepository tagDomainRepository;

    @Override
    public ModuleDomain createModuleWithTags(ModuleDomain moduleDomain) {
        // Convertir el conjunto de etiquetas a una cadena de IDs separados por comas
        String tagIds = moduleDomain.getTags()
                .stream()
                .map(TagDomain::getTagId)  // Cambiar de getTagName a getTagId
                .map(String::valueOf)      // Convertir los IDs a String
                .collect(Collectors.joining(","));

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_module_with_tags");
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_tags", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("module_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_title", moduleDomain.getTitle());
        query.setParameter("p_description", moduleDomain.getDescription());
        query.setParameter("p_tags", tagIds);  // Pasar los IDs en lugar de los nombres

        query.execute();

        Integer generatedId = (Integer) query.getOutputParameterValue("module_id");
        moduleDomain.setModuleId(generatedId);

        return moduleDomain;
    }

    @Override
    public List<ModuleDomain> getAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_modules_with_tags", ModuleEntity.class);
        query.execute();
        List<ModuleEntity> resultList = query.getResultList();
        return resultList.stream().map(ModuleDomain::fromEntityStatic).collect(Collectors.toList());
    }

    @Override
    public ModuleDomain updateModuleWithTags(ModuleDomain moduleDomain) {
        // Convertir el conjunto de etiquetas a una cadena de IDs separados por comas
        String tagIds = moduleDomain.getTags()
                .stream()
                .map(TagDomain::getTagId)  // Obtener los IDs de las etiquetas
                .map(String::valueOf)      // Convertirlos a String
                .collect(Collectors.joining(",")); // Unirlos con comas

        // Crear la consulta para llamar al procedimiento almacenado
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_module_with_tags");

        // Registrar parámetros del SP
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_tag_ids", String.class, ParameterMode.IN);

        // Asignar valores a los parámetros
        query.setParameter("p_module_id", moduleDomain.getModuleId());
        query.setParameter("p_title", moduleDomain.getTitle());
        query.setParameter("p_description", moduleDomain.getDescription());
        query.setParameter("p_tag_ids", tagIds);  // Pasar los IDs en lugar de nombres

        // Ejecutar el SP
        query.execute();

        return moduleDomain; // Retornar el módulo actualizado
    }

    @Override
    public Optional<ModuleDomain> getById(Integer moduleId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_module_by_id", ModuleEntity.class);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_module_id", moduleId);
        query.execute();
        List<ModuleEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        // Usamos el método estático para convertir la entidad al dominio
        return Optional.of(ModuleDomain.fromEntityStatic(resultList.get(0)));
    }

    @Override
    public List<ModuleDomain> filterModulesByTags(List<Integer> tagIds) {
        // Convertir la lista de IDs de etiquetas a una cadena separada por comas
        String tagIdsStr = tagIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Crear la consulta al procedimiento almacenado
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_search_modules_by_tags");

        // Registrar el parámetro de entrada
        query.registerStoredProcedureParameter("p_tag_ids", String.class, ParameterMode.IN);

        // Establecer el parámetro con la cadena de IDs
        query.setParameter("p_tag_ids", tagIdsStr);

        // Ejecutar la consulta
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
}
