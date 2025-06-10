package com.germogli.backend.community.post.infrastructure.crud;

import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio CRUD básico para PostEntity.
 */
public interface CommunityPostCrudRepository extends JpaRepository<PostEntity, Integer> {
    Optional<PostEntity> findById(Integer id);
}
