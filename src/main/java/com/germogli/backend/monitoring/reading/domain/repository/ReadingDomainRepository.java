package com.germogli.backend.monitoring.reading.domain.repository;

import com.germogli.backend.monitoring.reading.domain.model.ReadingDomain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Reading.
 * Define las operaciones básicas de persistencia para lecturas de sensores.
 */
public interface ReadingDomainRepository {
    /**
     * Guarda una lectura en la base de datos.
     *
     * @param reading Lectura a guardar.
     * @return Lectura guardada con su ID asignado.
     */
    ReadingDomain save(ReadingDomain reading);

    /**
     * Busca una lectura por su ID.
     *
     * @param id Identificador de la lectura.
     * @return Lectura encontrada o empty si no existe.
     */
    Optional<ReadingDomain> findById(Integer id);

    /**
     * Obtiene todas las lecturas.
     *
     * @return Lista de todas las lecturas.
     */
    List<ReadingDomain> findAll();

    /**
     * Elimina una lectura por su ID.
     *
     * @param id Identificador de la lectura a eliminar.
     */
    void deleteById(Integer id);

    /**
     * Encuentra todas las lecturas de un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Lista de lecturas del cultivo.
     */
    List<ReadingDomain> findByCropId(Integer cropId);

    /**
     * Encuentra todas las lecturas de un sensor específico.
     *
     * @param sensorId Identificador del sensor.
     * @return Lista de lecturas del sensor.
     */
    List<ReadingDomain> findBySensorId(Integer sensorId);

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @return Lista de lecturas del cultivo y sensor.
     */
    List<ReadingDomain> findByCropIdAndSensorId(Integer cropId, Integer sensorId);

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos en un rango de fechas.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @param limit Límite de registros a recuperar.
     * @return Lista de lecturas del cultivo y sensor en el rango de fechas.
     */
    List<ReadingDomain> findByCropIdAndSensorIdAndDateRange(
            Integer cropId, Integer sensorId, LocalDateTime startDate, LocalDateTime endDate, Integer limit);

    /**
     * Guarda un lote de lecturas en la base de datos.
     *
     * @param readings Lista de lecturas a guardar.
     * @return Lista de lecturas guardadas.
     */
    List<ReadingDomain> saveAll(List<ReadingDomain> readings);
}