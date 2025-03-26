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

import java.util.List;
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
}
