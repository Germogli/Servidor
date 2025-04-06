package com.germogli.backend.common.notification.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "notification_date", nullable = false)
    private LocalDateTime notificationDate;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;
}
