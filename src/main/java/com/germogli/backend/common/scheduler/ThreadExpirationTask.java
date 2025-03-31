package com.germogli.backend.common.scheduler;

import com.germogli.backend.community.thread.domain.service.ThreadDomainService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tarea programada para eliminar hilos expirados para pruebas.
 * Esta tarea se ejecuta cada 2 minutos y elimina aquellos hilos
 * cuyo campo creationDate más 2 minutos sea anterior a la fecha actual.
 */
@Component
public class ThreadExpirationTask {

    private final ThreadDomainService threadDomainService;

    public ThreadExpirationTask(ThreadDomainService threadDomainService) {
        this.threadDomainService = threadDomainService;
    }

    /**
     * Ejecuta la tarea cada 2 minutos.
     * Se eliminan los hilos que tengan más de 2 minutos de creación.
     */
    @Scheduled(cron = "0 0/2 * * * *")
    @Transactional
    public void removeExpiredThreads() {
        LocalDateTime now = LocalDateTime.now();
        threadDomainService.getAllThreads().stream()
                .filter(thread -> thread.getCreationDate().plusDays(2).isBefore(now))
                .forEach(thread -> threadDomainService.deleteThreadAsSystem(thread.getId()));
    }
}
