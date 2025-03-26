package com.germogli.backend.education.module.infrastructure.repository;

import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.repository.ModuleDomainRepository;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("educationModuleRepository")
@RequiredArgsConstructor
public class ModuleRepository implements ModuleDomainRepository {

    private final EntityManager entityManager;
    private final TagDomainRepository tagDomainRepository;

    @Override
    public ModuleDomain createModuleWithTags(ModuleDomain moduleDomain) {
        try {
            // Establecer la fecha de creación si está nula
            if (moduleDomain.getCreationDate() == null) {
                moduleDomain.setCreationDate(LocalDateTime.now());
            }
            // Crear el StoredProcedureQuery para el procedimiento almacenado
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_module_with_tags");

            // Registrar parámetros de entrada y salida
            query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_tags", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("module_id", Integer.class, ParameterMode.OUT);

            // Convertir la lista de etiquetas a una cadena separada por comas usando solo los nombres
            String tags = moduleDomain.getTags().stream()
                    .map(TagDomain::getTagName)  // Usar solo el nombre de la etiqueta
                    .collect(Collectors.joining(","));

            // Asignar los valores de los parámetros
            query.setParameter("p_title", moduleDomain.getTitle());
            query.setParameter("p_description", moduleDomain.getDescription());
            query.setParameter("p_tags", tags);

            // Ejecutar el procedimiento almacenado
            query.execute();

            // Obtener el ID generado para el módulo
            Integer moduleId = (Integer) query.getOutputParameterValue("module_id");

            // Verificar si se generó correctamente el módulo
            if (moduleId == null) {
                throw new RuntimeException("No se pudo crear el módulo");
            }

            Set<TagDomain> persistedTags = moduleDomain.getTags().stream()
                    .map(tag -> {
                        // Buscar la etiqueta por su nombre en la base de datos
                        TagDomain tagDomain = tagDomainRepository.getById(tag.getTagId());

                        if (tagDomain == null) {
                            throw new ResourceNotFoundException("Etiqueta no encontrada: " + tag.getTagName());
                        }

                        return tagDomain;
                    })
                    .collect(Collectors.toSet());

            // Construir el objeto ModuleDomain con el ID generado y las etiquetas persistidas
            return ModuleDomain.builder()
                    .moduleId(moduleId)
                    .title(moduleDomain.getTitle())
                    .description(moduleDomain.getDescription())
                    .creationDate(moduleDomain.getCreationDate())
                    .tags(persistedTags)
                    .build();
        } catch (Exception e) {
            // Manejo genérico de excepciones utilizando ExceptionHandlerUtil
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al crear el modulo: " + moduleDomain.getTitle());
        }
    }
}
