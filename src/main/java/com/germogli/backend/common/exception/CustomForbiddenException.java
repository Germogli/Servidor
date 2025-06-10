package com.germogli.backend.common.exception;

/**
 * Excepción personalizada para indicar que la acción está prohibida.
 * Se utiliza para devolver un error más específico que una simple RuntimeException.
 */
public class CustomForbiddenException extends RuntimeException {
    public CustomForbiddenException(String message) {
        super(message);
    }
}
