package com.germogli.backend.community.comment.infrastructure.crud;

import com.germogli.backend.community.comment.infrastructure.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio CRUD básico para CommentEntity.
 * Se utiliza para operaciones básicas en casos puntuales.
 */
public interface CommunityCommentCrudRepository extends JpaRepository<CommentEntity, Integer> {
    Optional<CommentEntity> findById(Integer id);
}
