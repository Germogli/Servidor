package com.germogli.backend.authentication.infrastructure.crud;

import com.germogli.backend.authentication.infrastructure.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByExpirationDateBefore(LocalDateTime now);
}