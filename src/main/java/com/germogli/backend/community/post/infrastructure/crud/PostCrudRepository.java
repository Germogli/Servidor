package com.germogli.backend.community.post.infrastructure.crud;

import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio CRUD para la entidad PostEntity.
 */
public interface PostCrudRepository extends JpaRepository<PostEntity, Integer> {
}
