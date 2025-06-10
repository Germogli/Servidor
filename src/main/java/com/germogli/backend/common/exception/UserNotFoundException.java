package com.germogli.backend.common.exception;

/**
 * Excepción que se lanza cuando no se encuentra un usuario solicitado.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
