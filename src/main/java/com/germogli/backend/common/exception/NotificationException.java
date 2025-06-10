package com.germogli.backend.common.exception;

/**
 * Excepción personalizada para errores en el envío de notificaciones.
 */
public class NotificationException extends RuntimeException {
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
