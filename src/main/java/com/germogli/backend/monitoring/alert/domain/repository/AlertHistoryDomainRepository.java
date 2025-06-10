package com.germogli.backend.monitoring.alert.domain.repository;

import com.germogli.backend.monitoring.alert.domain.model.AlertHistoryDomain;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del historial de alertas.
 */
public interface AlertHistoryDomainRepository {
    /**
     * Guarda un registro de historial de alerta en la base de datos.
     *
     * @param alertHistory Historial de alerta a guardar.
     * @return Historial de alerta guardado con su ID asignado.
     */
    AlertHistoryDomain save(AlertHistoryDomain alertHistory);

    /**
     * Busca un registro de historial de alerta por su ID.
     *
     * @param id Identificador del historial de alerta.
     * @return Historial de alerta encontrado o empty si no existe.
     */
    Optional<AlertHistoryDomain> findById(Integer id);

    /**
     * Obtiene todos los registros de historial de alertas.
     *
     * @return Lista de todos los historiales de alertas.
     */
    List<AlertHistoryDomain> findAll();

    /**
     * Elimina un registro de historial de alerta por su ID.
     *
     * @param id Identificador del historial de alerta a eliminar.
     */
    void deleteById(Integer id);

    /**
     * Encuentra todos los registros de historial de alertas de un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Lista de historiales de alertas del cultivo.
     */
    List<AlertHistoryDomain> findByCropId(Integer cropId);

    /**
     * Encuentra todos los registros de historial de alertas de un nivel específico.
     *
     * @param alertLevel Nivel de alerta.
     * @return Lista de historiales de alertas del nivel especificado.
     */
    List<AlertHistoryDomain> findByAlertLevel(String alertLevel);

    /**
     * Encuentra todos los registros de historial de alertas de un usuario específico.
     *
     * @param userId Identificador del usuario.
     * @return Lista de historiales de alertas del usuario.
     */
    List<AlertHistoryDomain> findByUserId(Integer userId);

    /**
     * Actualiza el estado de un registro de historial de alerta.
     *
     * @param id Identificador del historial de alerta.
     * @param status Nuevo estado.
     * @param resolutionDate Fecha de resolución.
     * @param comments Comentarios.
     * @return Historial de alerta actualizado.
     */
    AlertHistoryDomain updateStatus(Integer id, String status, LocalDateTime resolutionDate, String comments);
}