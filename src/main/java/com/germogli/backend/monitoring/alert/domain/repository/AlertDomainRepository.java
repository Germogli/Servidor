package com.germogli.backend.monitoring.alert.domain.repository;

import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Alert.
 * Define las operaciones básicas de persistencia para alertas.
 */
public interface AlertDomainRepository {
    /**
     * Guarda una alerta en la base de datos.
     *
     * @param alert Alerta a guardar.
     * @return Alerta guardada con su ID asignado.
     */
    AlertDomain save(AlertDomain alert);

    /**
     * Busca una alerta por su ID.
     *
     * @param id Identificador de la alerta.
     * @return Alerta encontrada o empty si no existe.
     */
    Optional<AlertDomain> findById(Integer id);

    /**
     * Obtiene todas las alertas.
     *
     * @return Lista de todas las alertas.
     */
    List<AlertDomain> findAll();

    /**
     * Elimina una alerta por su ID.
     *
     * @param id Identificador de la alerta a eliminar.
     */
    void deleteById(Integer id);

    /**
     * Encuentra todas las alertas de un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Lista de alertas del cultivo.
     */
    List<AlertDomain> findByCropId(Integer cropId);

    /**
     * Encuentra todas las alertas de un nivel específico.
     *
     * @param alertLevel Nivel de alerta.
     * @return Lista de alertas del nivel especificado.
     */
    List<AlertDomain> findByAlertLevel(String alertLevel);

    /**
     * Encuentra todas las alertas de un cultivo específico en un rango de fechas.
     *
     * @param cropId Identificador del cultivo.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @return Lista de alertas del cultivo en el rango de fechas.
     */
    List<AlertDomain> findByCropIdAndDateRange(Integer cropId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Procesa una alerta según umbrales predefinidos y genera el historial correspondiente.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param alertLevel Nivel de alerta.
     * @param alertMessage Mensaje de alerta.
     * @return Alerta procesada.
     */
    AlertDomain processAlert(Integer cropId, Integer sensorId, String alertLevel, String alertMessage);
}