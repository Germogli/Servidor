package com.germogli.backend.community.notification.domain.model;

import com.germogli.backend.community.domain.model.BaseCommunityResource;
import com.germogli.backend.community.domain.model.Converter;
import com.germogli.backend.community.notification.infrastructure.entity.NotificationEntity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Modelo de dominio para una notificación.
 * Representa la lógica y los datos asociados a una notificación en el contexto de Community.
 * Implementa la interfaz Converter para estandarizar la conversión entre NotificationEntity y NotificationDomain.
 */
@Data
@SuperBuilder
public class NotificationDomain extends BaseCommunityResource implements Converter<NotificationDomain, NotificationEntity> {
    private Integer id;
    private Integer userId;
    private String message;
    private String category;

    /**
     * Método de instancia requerido por la interfaz Converter.
     * Delegamos en el método estático para permitir el uso de referencias.
     *
     * @param entity Entidad a convertir.
     * @return Objeto NotificationDomain con los datos de la entidad.
     */
    @Override
    public NotificationDomain fromEntity(NotificationEntity entity) {
        return fromEntityStatic(entity);
    }

    /**
     * Método estático para convertir una entidad NotificationEntity en un objeto NotificationDomain.
     * Permite usar la referencia de método: NotificationDomain::fromEntityStatic.
     *
     * @param entity Entidad a convertir.
     * @return Objeto NotificationDomain.
     */
    public static NotificationDomain fromEntityStatic(NotificationEntity entity) {
        return NotificationDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .message(entity.getMessage())
                .category(entity.getCategory())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto NotificationDomain en una entidad NotificationEntity para persistencia.
     *
     * @return Objeto NotificationEntity con los datos de este modelo.
     */
    @Override
    public NotificationEntity toEntity() {
        return NotificationEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .message(this.message)
                .category(this.category)
                .creationDate(this.getCreationDate())
                .build();
    }
}
