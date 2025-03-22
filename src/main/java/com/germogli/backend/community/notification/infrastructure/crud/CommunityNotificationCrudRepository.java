package com.germogli.backend.community.notification.infrastructure.crud;

import com.germogli.backend.community.notification.infrastructure.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio CRUD básico para NotificationEntity.
 * Útil para operaciones simples que no requieran procedimientos almacenados.
 */
public interface CommunityNotificationCrudRepository extends JpaRepository<NotificationEntity, Integer> {
    Optional<NotificationEntity> findById(Integer id);
}
