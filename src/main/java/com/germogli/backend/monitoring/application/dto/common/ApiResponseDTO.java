package com.germogli.backend.monitoring.application.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para envolver las respuestas de la API.
 * Proporciona un formato consistente para la comunicación.
 *
 * @param <T> Tipo de dato que se retorna en la respuesta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    /**
     * Mensaje descriptivo de la respuesta.
     */
    private String message;
    /**
     * Datos devueltos en la respuesta.
     */
    private T data;
}
