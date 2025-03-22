package com.germogli.backend.community.group.infrastructure.repository;

import com.germogli.backend.community.group.domain.model.GroupDomain;
import com.germogli.backend.community.group.domain.repository.GroupDomainRepository;
import com.germogli.backend.community.group.infrastructure.entity.GroupEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio para el dominio Group utilizando procedimientos almacenados.
 */
@Repository("communityGroupRepository")
@RequiredArgsConstructor
public class GroupRepository implements GroupDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un grupo.
     * - Si el grupo es nuevo (ID nulo), se crea mediante sp_create_group.
     * - Si ya existe, se actualiza mediante sp_update_group.
     */
    @Override
    @Transactional
    public GroupDomain save(GroupDomain group) {
        if (group.getId() == null) {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_group", GroupEntity.class);
            query.registerStoredProcedureParameter("p_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
            query.setParameter("p_name", group.getName());
            query.setParameter("p_description", group.getDescription());
            query.execute();
            return group;
        } else {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_group");
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);
            query.setParameter("p_group_id", group.getId());
            query.setParameter("p_name", group.getName());
            query.setParameter("p_description", group.getDescription());
            query.execute();
            return group;
        }
    }

    /**
     * Busca un grupo por su ID mediante sp_get_group_by_id.
     */
    @Override
    public Optional<GroupDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_group_by_id", GroupEntity.class);
        query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_group_id", id);
        query.execute();
        List<GroupEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        // Se utiliza el método estático fromEntityStatic para convertir la entidad en el dominio.
        return Optional.of(GroupDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todos los grupos mediante sp_get_all_groups.
     */
    @Override
    public List<GroupDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_groups", GroupEntity.class);
        query.execute();
        List<GroupEntity> resultList = query.getResultList();
        return resultList.stream().map(GroupDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un grupo mediante sp_delete_group.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_group");
        query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_group_id", id);
        query.execute();
    }

    /**
     * Verifica la existencia de un grupo mediante sp_exists_group.
     */
    @Override
    public boolean existsById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_exists_group", Long.class);
        query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_group_id", id);
        query.execute();
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
}
