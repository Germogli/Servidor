package com.germogli.backend.common.scheduler;

import com.germogli.backend.community.thread.domain.service.ThreadDomainService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tarea programada para eliminar hilos expirados.
 * Esta tarea se ejecuta una vez al día y elimina aquellos hilos
 * cuyo campo creationDate más 2 días sea anterior a la fecha actual.
 */
@Component
public class ThreadExpirationTask {

    private final ThreadDomainService threadDomainService;

    public ThreadExpirationTask(ThreadDomainService threadDomainService) {
        this.threadDomainService = threadDomainService;
    }

    /**
     * Ejecuta la tarea una vez al día a medianoche.
     * Se eliminan los hilos que tengan más de 2 días de creación.
     */
    @Scheduled(cron = "0 0 0 * * *") // Ejecuta cada día a medianoche
    @Transactional
    public void removeExpiredThreads() {
        LocalDateTime now = LocalDateTime.now();
        threadDomainService.getAllThreads().stream()
                .filter(thread -> thread.getCreationDate().plusDays(2).isBefore(now))
                .forEach(thread -> threadDomainService.deleteThreadAsSystem(thread.getId()));
    }
}
