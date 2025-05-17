package com.germogli.backend.monitoring.sensor.domain.repository;

import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Sensor.
 * Define las operaciones básicas de persistencia para sensores.
 */
public interface SensorDomainRepository {
    /**
     * Guarda un sensor en la base de datos.
     *
     * @param sensor Sensor a guardar.
     * @return Sensor guardado con su ID asignado.
     */
    SensorDomain save(SensorDomain sensor);

    /**
     * Busca un sensor por su ID.
     *
     * @param id Identificador del sensor.
     * @return Sensor encontrado o empty si no existe.
     */
    Optional<SensorDomain> findById(Integer id);

    /**
     * Obtiene todos los sensores.
     *
     * @return Lista de todos los sensores.
     */
    List<SensorDomain> findAll();

    /**
     * Elimina un sensor por su ID.
     *
     * @param id Identificador del sensor a eliminar.
     */
    void deleteById(Integer id);

    /**
     * Encuentra todos los sensores asociados a un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Lista de sensores asociados al cultivo.
     */
    List<SensorDomain> findByCropId(Integer cropId);

    /**
     * Asocia un sensor a un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     */
    void addSensorToCrop(Integer cropId, Integer sensorId);

    /**
     * Asocia un sensor a un cultivo con umbrales personalizados.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Umbral mínimo.
     * @param maxThreshold Umbral máximo.
     */
    void addSensorToCropWithThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold);

    /**
     * Actualiza los umbrales de un sensor asociado a un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Nuevo umbral mínimo.
     * @param maxThreshold Nuevo umbral máximo.
     */
    void updateSensorThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold);

    /**
     * Desasocia un sensor de un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     */
    void removeSensorFromCrop(Integer cropId, Integer sensorId);
    /**
     * Crea un nuevo sensor y lo asocia inmediatamente a un cultivo con umbrales personalizados.
     *
     * @param sensorType Tipo de sensor
     * @param unitOfMeasurement Unidad de medida
     * @param cropId ID del cultivo a asociar
     * @param minThreshold Valor mínimo aceptable
     * @param maxThreshold Valor máximo aceptable
     * @return Objeto SensorDomain creado
     */
    SensorDomain createAndAssociateToCrop(String sensorType, String unitOfMeasurement,
                                          Integer cropId, BigDecimal minThreshold,
                                          BigDecimal maxThreshold);

}