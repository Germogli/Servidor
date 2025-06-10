package com.germogli.backend.common.notification.domain.model;

import com.germogli.backend.common.notification.infrastructure.entity.NotificationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para una notificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDomain {
    private Integer id;
    private Integer userId;
    private String message;
    private String category;
    private LocalDateTime notificationDate;
    private Boolean isRead;

    /**
     * Convierte una entidad en un objeto de dominio.
     */
    public static NotificationDomain fromEntity(NotificationEntity entity) {
        return NotificationDomain.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .message(entity.getMessage())
                .category(entity.getCategory())
                .notificationDate(entity.getNotificationDate())
                .isRead(entity.getIsRead())
                .build();
    }

    /**
     * Convierte este objeto de dominio en una entidad para persistencia.
     */
    public NotificationEntity toEntity() {
        return NotificationEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .message(this.message)
                .category(this.category)
                .notificationDate(this.notificationDate)
                .isRead(this.isRead)
                .build();
    }
}
