package com.germogli.backend.monitoring.sensor.infrastructure.repository;

import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;
import com.germogli.backend.monitoring.sensor.domain.repository.SensorDomainRepository;
import com.germogli.backend.monitoring.sensor.infrastructure.entity.SensorEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de SensorDomainRepository utilizando procedimientos almacenados.
 */
@Repository
@RequiredArgsConstructor
public class SensorRepository implements SensorDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un sensor utilizando procedimientos almacenados.
     */
    @Override
    @Transactional
    public SensorDomain save(SensorDomain sensor) {
        if (sensor.getId() == null) {
            // Crear sensor usando sp_create_sensor
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_sensor");
            query.registerStoredProcedureParameter("p_sensor_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_unit_of_measurement", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.OUT);

            query.setParameter("p_sensor_type", sensor.getSensorType());
            query.setParameter("p_unit_of_measurement", sensor.getUnitOfMeasurement());

            query.execute();

            // Obtener el ID generado
            Integer sensorId = (Integer) query.getOutputParameterValue("p_sensor_id");
            sensor.setId(sensorId);

            return sensor;
        } else {
            // Actualizar sensor usando sp_update_sensor
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_sensor");
            query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_sensor_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_unit_of_measurement", String.class, ParameterMode.IN);

            query.setParameter("p_sensor_id", sensor.getId());
            query.setParameter("p_sensor_type", sensor.getSensorType());
            query.setParameter("p_unit_of_measurement", sensor.getUnitOfMeasurement());

            query.execute();

            return sensor;
        }
    }

    /**
     * Busca un sensor por su ID utilizando sp_get_sensor_by_id.
     */
    @Override
    public Optional<SensorDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_sensor_by_id", SensorEntity.class);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_sensor_id", id);
        query.execute();

        List<SensorEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(SensorDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todos los sensores utilizando sp_get_all_sensors.
     */
    @Override
    public List<SensorDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_sensors", SensorEntity.class);
        query.execute();

        List<SensorEntity> resultList = query.getResultList();
        return resultList.stream().map(SensorDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un sensor por su ID utilizando sp_delete_sensor.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_sensor");
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_sensor_id", id);
        query.execute();
    }

    /**
     * Encuentra todos los sensores asociados a un cultivo usando sp_get_sensors_by_crop_id.
     */
    @Override
    public List<SensorDomain> findByCropId(Integer cropId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_sensors_by_crop_id", SensorEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.execute();

        List<SensorEntity> resultList = query.getResultList();
        return resultList.stream().map(SensorDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Asocia un sensor a un cultivo usando sp_add_sensor_to_crop.
     */
    @Override
    @Transactional
    public void addSensorToCrop(Integer cropId, Integer sensorId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_add_sensor_to_crop");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.execute();
    }
    /**
     * Asocia un sensor a un cultivo con umbrales personalizados usando sp_add_sensor_to_crop_with_thresholds.
     */
    @Override
    @Transactional
    public void addSensorToCropWithThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_add_sensor_to_crop_with_thresholds");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_min_threshold", BigDecimal.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_max_threshold", BigDecimal.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_min_threshold", minThreshold);
        query.setParameter("p_max_threshold", maxThreshold);

        query.execute();
    }

    /**
     * Actualiza los umbrales de un sensor asociado a un cultivo usando sp_update_crop_sensor_thresholds.
     */
    @Override
    @Transactional
    public void updateSensorThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_crop_sensor_thresholds");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_min_threshold", BigDecimal.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_max_threshold", BigDecimal.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_min_threshold", minThreshold);
        query.setParameter("p_max_threshold", maxThreshold);

        query.execute();
    }

    /**
     * Desasocia un sensor de un cultivo usando sp_remove_sensor_from_crop.
     */
    @Override
    @Transactional
    public void removeSensorFromCrop(Integer cropId, Integer sensorId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_remove_sensor_from_crop");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.execute();
    }
    @Override
    @Transactional
    public SensorDomain createAndAssociateToCrop(String sensorType, String unitOfMeasurement,
                                                 Integer cropId, BigDecimal minThreshold,
                                                 BigDecimal maxThreshold) {

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_sensor_with_association");
        query.registerStoredProcedureParameter("p_sensor_type", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_unit_of_measurement", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_min_threshold", BigDecimal.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_max_threshold", BigDecimal.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_sensor_type", sensorType);
        query.setParameter("p_unit_of_measurement", unitOfMeasurement);
        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_min_threshold", minThreshold);
        query.setParameter("p_max_threshold", maxThreshold);

        query.execute();

        // Obtener el ID generado
        Integer sensorId = (Integer) query.getOutputParameterValue("p_sensor_id");

        // Crear y devolver el objeto de dominio
        return SensorDomain.builder()
                .id(sensorId)
                .sensorType(sensorType)
                .unitOfMeasurement(unitOfMeasurement)
                .build();
    }
}