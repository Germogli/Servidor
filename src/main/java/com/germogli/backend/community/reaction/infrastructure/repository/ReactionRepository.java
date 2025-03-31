package com.germogli.backend.community.reaction.infrastructure.repository;

import com.germogli.backend.community.reaction.domain.model.ReactionDomain;
import com.germogli.backend.community.reaction.domain.repository.ReactionDomainRepository;
import com.germogli.backend.community.reaction.infrastructure.entity.ReactionEntity;
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
 * Implementación del repositorio para el dominio Reaction utilizando procedimientos almacenados.
 */
@Repository("communityReactionRepository")
@RequiredArgsConstructor
public class ReactionRepository implements ReactionDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza una reacción.
     * Si la reacción es nueva (ID nulo), se crea mediante sp_add_reaction;
     * de lo contrario, se actualiza mediante sp_update_reaction.
     */
    @Override
    @Transactional
    public ReactionDomain save(ReactionDomain reaction) {
        if (reaction.getId() == null) {
            // Crear reacción mediante sp_add_reaction
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_add_reaction", ReactionEntity.class);
            query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_reaction_type", String.class, ParameterMode.IN);
            query.setParameter("p_post_id", reaction.getPostId());
            query.setParameter("p_user_id", reaction.getUserId());
            query.setParameter("p_reaction_type", reaction.getReactionType());
            query.execute();
            return reaction;
        } else {
            // Actualizar reacción mediante sp_update_reaction
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_reaction", ReactionEntity.class);
            query.registerStoredProcedureParameter("p_reaction_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_reaction_type", String.class, ParameterMode.IN);
            query.setParameter("p_reaction_id", reaction.getId());
            query.setParameter("p_reaction_type", reaction.getReactionType());
            query.execute();
            return reaction;
        }
    }

    /**
     * Busca una reacción por su ID mediante sp_get_reaction_by_id.
     */
    @Override
    public Optional<ReactionDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_reaction_by_id", ReactionEntity.class);
        query.registerStoredProcedureParameter("p_reaction_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reaction_id", id);
        query.execute();
        List<ReactionEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ReactionDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todas las reacciones mediante sp_get_all_reactions.
     */
    @Override
    public List<ReactionDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_reactions", ReactionEntity.class);
        query.execute();
        List<ReactionEntity> resultList = query.getResultList();
        return resultList.stream().map(ReactionDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una reacción mediante sp_delete_reaction.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_reaction");
        query.registerStoredProcedureParameter("p_reaction_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reaction_id", id);
        query.execute();
    }
}
