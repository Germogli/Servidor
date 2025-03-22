package com.germogli.backend.common.exception;

/**
 * Excepción que se lanza cuando se intenta crear un usuario que ya existe.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
