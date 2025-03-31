package com.germogli.backend.common.scheduler;

import com.germogli.backend.authentication.infrastructure.crud.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PasswordResetTokenCleanupTask {

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetTokenCleanupTask(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Tarea programada para eliminar los tokens expirados.
     * Se ejecuta cada hora y elimina todos los tokens cuya fecha de expiración sea anterior al momento actual.
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora a la hora en punto
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpirationDateBefore(now);
    }
}