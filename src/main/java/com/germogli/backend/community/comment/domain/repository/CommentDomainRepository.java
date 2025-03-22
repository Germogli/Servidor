package com.germogli.backend.community.comment.domain.repository;

import com.germogli.backend.community.comment.domain.model.CommentDomain;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de persistencia para el dominio Comment.
 */
public interface CommentDomainRepository {
    CommentDomain save(CommentDomain comment);
    Optional<CommentDomain> findById(Integer id);
    List<CommentDomain> findAll();
    void deleteById(Integer id);
}
