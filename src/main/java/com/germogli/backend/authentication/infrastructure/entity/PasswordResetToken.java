package com.germogli.backend.authentication.infrastructure.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @Column(length = 100)
    private String token;  // Usamos UUID como token

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
}