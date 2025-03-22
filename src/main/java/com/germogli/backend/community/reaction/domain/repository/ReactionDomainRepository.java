package com.germogli.backend.community.reaction.domain.repository;

import com.germogli.backend.community.reaction.domain.model.ReactionDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Reaction.
 */
public interface ReactionDomainRepository {
    ReactionDomain save(ReactionDomain reaction);
    Optional<ReactionDomain> findById(Integer id);
    List<ReactionDomain> findAll();
    void deleteById(Integer id);
}
