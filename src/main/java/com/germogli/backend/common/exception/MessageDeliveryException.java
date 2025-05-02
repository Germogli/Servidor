package com.germogli.backend.common.exception;

/**
 * Excepción que se lanza cuando hay problemas en la entrega de mensajes en tiempo real.
 */
public class MessageDeliveryException extends RuntimeException {

    public MessageDeliveryException(String message) {
        super(message);
    }

    public MessageDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}