package com.germogli.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para manejar excepciones en el proyecto.
 * Centraliza la captura y el logging de errores en los repositorios.
 */
public class ExceptionHandlerUtil {

    /**
     * Método estático que maneja excepciones de forma centralizada.
     *
     * @param clazz   Clase donde ocurrió la excepción (se usa para el logger).
     * @param e       Excepción que se capturó.
     * @param message Mensaje descriptivo del error.
     * @return Una excepción personalizada `DatabaseOperationException`.
     */
    public static <T> RuntimeException handleException(Class<T> clazz, Exception e, String message) {
        // Crea un logger específico para la clase donde ocurrió el error
        Logger logger = LoggerFactory.getLogger(clazz);

        // Registra el error con el mensaje personalizado y la causa específica
        logger.error("{} - Causa: {}", message, e.getMessage());

        // Retorna una nueva excepción personalizada con el mensaje y la causa original
        return new DatabaseOperationException(message, e);
    }
}