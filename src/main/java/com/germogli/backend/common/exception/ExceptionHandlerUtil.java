package com.germogli.backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para manejar excepciones de forma centralizada.
 * Registra el error y retorna una DatabaseOperationException con información detallada.
 */
public class ExceptionHandlerUtil {

    /**
     * Maneja una excepción, registrando la causa y devolviendo una DatabaseOperationException.
     *
     * @param clazz   Clase donde ocurrió la excepción, usada para el logger.
     * @param e       Excepción capturada.
     * @param message Mensaje descriptivo del error.
     * @return Una excepción DatabaseOperationException con el mensaje y la causa original.
     */
    public static <T> RuntimeException handleException(Class<T> clazz, Exception e, String message) {
        // Crear un logger específico para la clase donde ocurrió el error
        Logger logger = LoggerFactory.getLogger(clazz);
        // Registra el error con el mensaje y la causa
        logger.error("{} - Causa: {}", message, e.getMessage());
        // Retorna una nueva excepción con el mensaje y la causa original
        return new DatabaseOperationException(message, e);
    }
}
