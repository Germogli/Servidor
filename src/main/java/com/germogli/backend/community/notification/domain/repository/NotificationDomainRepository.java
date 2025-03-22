package com.germogli.backend.community.notification.domain.repository;

import com.germogli.backend.community.notification.domain.model.NotificationDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Notification.
 */
public interface NotificationDomainRepository {
    NotificationDomain save(NotificationDomain notification);
    Optional<NotificationDomain> findById(Integer id);
    List<NotificationDomain> findByUserId(Integer userId);
    List<NotificationDomain> findAll();
    void deleteById(Integer id);
}
