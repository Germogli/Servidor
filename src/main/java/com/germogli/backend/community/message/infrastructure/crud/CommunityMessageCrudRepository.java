package com.germogli.backend.community.message.infrastructure.crud;

import com.germogli.backend.community.message.infrastructure.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio CRUD básico para MessageEntity.
 * Se utiliza para operaciones básicas en casos puntuales.
 */
public interface CommunityMessageCrudRepository extends JpaRepository<MessageEntity, Integer> {
    Optional<MessageEntity> findById(Integer id);
}
