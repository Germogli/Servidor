package com.germogli.backend.common.exception;

/**
 * Excepción personalizada para representar errores durante operaciones de base de datos.
 * Permite encapsular la causa original del error para un mejor diagnóstico.
 */
public class DatabaseOperationException extends RuntimeException {
    public DatabaseOperationException(String message) {
        super(message);
    }
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
