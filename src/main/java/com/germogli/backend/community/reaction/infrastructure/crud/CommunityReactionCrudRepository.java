package com.germogli.backend.community.reaction.infrastructure.crud;

import com.germogli.backend.community.reaction.infrastructure.entity.ReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio CRUD básico para ReactionEntity.
 */
public interface CommunityReactionCrudRepository extends JpaRepository<ReactionEntity, Integer> {
    Optional<ReactionEntity> findById(Integer id);
}
