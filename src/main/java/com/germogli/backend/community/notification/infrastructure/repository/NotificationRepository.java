package com.germogli.backend.community.notification.infrastructure.repository;

import com.germogli.backend.community.notification.domain.model.NotificationDomain;
import com.germogli.backend.community.notification.domain.repository.NotificationDomainRepository;
import com.germogli.backend.community.notification.infrastructure.entity.NotificationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio para el dominio Notification utilizando procedimientos almacenados.
 */
@Repository("communityNotificationRepository")
@RequiredArgsConstructor
public class NotificationRepository implements NotificationDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda una notificación.
     * Si es nueva (ID nulo), se crea mediante sp_create_notification.
     * En caso contrario, se actualiza utilizando entityManager.find (no se dispone de SP para actualizar).
     */
    @Override
    @Transactional
    public NotificationDomain save(NotificationDomain notification) {
        if (notification.getId() == null) {
            // Crear notificación mediante sp_create_notification
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_notification", NotificationEntity.class);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_message", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_category", String.class, ParameterMode.IN);
            query.setParameter("p_user_id", notification.getUserId());
            query.setParameter("p_message", notification.getMessage());
            query.setParameter("p_category", notification.getCategory());
            query.execute();
            return notification;
        } else {
            // Actualizar notificación (si se requiriera, se utiliza find)
            NotificationEntity entity = entityManager.find(NotificationEntity.class, notification.getId());
            if (entity != null) {
                entity.setMessage(notification.getMessage());
                entity.setCategory(notification.getCategory());
                return NotificationDomain.fromEntityStatic(entity);
            } else {
                return notification;
            }
        }
    }

    /**
     * Busca una notificación por su ID utilizando entityManager.find.
     */
    @Override
    public Optional<NotificationDomain> findById(Integer id) {
        NotificationEntity entity = entityManager.find(NotificationEntity.class, id);
        return entity != null ? Optional.of(NotificationDomain.fromEntityStatic(entity)) : Optional.empty();
    }

    /**
     * Obtiene las notificaciones de un usuario mediante sp_get_notifications_by_user.
     */
    @Override
    public List<NotificationDomain> findByUserId(Integer userId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_notifications_by_user", NotificationEntity.class);
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_user_id", userId);
        query.execute();
        List<NotificationEntity> entities = query.getResultList();
        return entities.stream().map(NotificationDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Obtiene todas las notificaciones mediante una consulta JPQL.
     */
    @Override
    public List<NotificationDomain> findAll() {
        List<NotificationEntity> entities = entityManager.createQuery("SELECT n FROM CommunityNotificationEntity n", NotificationEntity.class)
                .getResultList();
        return entities.stream().map(NotificationDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una notificación mediante sp_delete_notification.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_notification");
        query.registerStoredProcedureParameter("p_notification_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_notification_id", id);
        query.execute();
    }
}
