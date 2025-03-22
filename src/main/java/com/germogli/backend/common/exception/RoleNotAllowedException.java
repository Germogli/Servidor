package com.germogli.backend.common.exception;

/**
 * Excepción que se lanza cuando un usuario intenta realizar una acción para la cual no tiene permisos.
 */
public class RoleNotAllowedException extends RuntimeException {
    public RoleNotAllowedException(String message) {
        super(message);
    }
}
