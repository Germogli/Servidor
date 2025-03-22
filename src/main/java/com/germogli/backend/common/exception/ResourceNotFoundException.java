package com.germogli.backend.common.exception;

/**
 * Excepción que se lanza cuando no se encuentra un recurso solicitado.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
