package com.germogli.backend.common.notification.infrastructure.repository;

import com.germogli.backend.common.notification.infrastructure.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
   // List<NotificationEntity> findByUserId(Integer userId);
}
