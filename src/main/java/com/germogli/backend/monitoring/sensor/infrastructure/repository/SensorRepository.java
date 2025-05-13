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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de SensorDomainRepository utilizando procedimientos almacenados
 * y consultas JPA.
 */
@Repository
@RequiredArgsConstructor
public class SensorRepository implements SensorDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un sensor.
     * Utiliza el procedimiento almacenado sp_create_sensor para crear nuevos sensores.
     */
    @Override
    @Transactional
    public SensorDomain save(SensorDomain sensor) {
        if (sensor.getId() == null) {
            // Crear sensor usando sp_create_sensor
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_sensor");
            query.registerStoredProcedureParameter("p_sensor_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_unit_of_measurement", String.class, ParameterMode.IN);

            query.setParameter("p_sensor_type", sensor.getSensorType());
            query.setParameter("p_unit_of_measurement", sensor.getUnitOfMeasurement());

            query.execute();

            // Obtener el último ID insertado
            Integer lastInsertId = (Integer) entityManager.createNativeQuery(
                            "SELECT LAST_INSERT_ID()")
                    .getSingleResult();

            sensor.setId(lastInsertId);
            return sensor;
        } else {
            // Para actualización, usar JPA directamente ya que no hay un procedimiento almacenado específico
            SensorEntity entity = entityManager.find(SensorEntity.class, sensor.getId());
            if (entity != null) {
                entity.setSensorType(sensor.getSensorType());
                entity.setUnitOfMeasurement(sensor.getUnitOfMeasurement());
                entityManager.merge(entity);
            }
            return sensor;
        }
    }

    /**
     * Busca un sensor por su ID.
     */
    @Override
    public Optional<SensorDomain> findById(Integer id) {
        SensorEntity entity = entityManager.find(SensorEntity.class, id);
        return Optional.ofNullable(entity).map(SensorDomain::fromEntityStatic);
    }

    /**
     * Obtiene todos los sensores.
     */
    @Override
    public List<SensorDomain> findAll() {
        List<SensorEntity> entities = entityManager.createQuery(
                        "SELECT s FROM SensorEntity s", SensorEntity.class)
                .getResultList();
        return entities.stream().map(SensorDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un sensor por su ID.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        // Primero eliminar de la tabla de relación
        entityManager.createQuery("DELETE FROM CropSensorEntity cs WHERE cs.id.sensorId = :sensorId")
                .setParameter("sensorId", id)
                .executeUpdate();

        // Luego eliminar el sensor
        entityManager.createQuery("DELETE FROM SensorEntity s WHERE s.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Encuentra todos los sensores asociados a un cultivo específico.
     */
    @Override
    public List<SensorDomain> findByCropId(Integer cropId) {
        // Consulta nativa para obtener los sensores de un cultivo específico
        List<SensorEntity> entities = entityManager.createNativeQuery(
                        "SELECT s.* FROM sensors s " +
                                "JOIN crop_sensors cs ON s.sensor_id = cs.sensor_id " +
                                "WHERE cs.crop_id = :cropId", SensorEntity.class)
                .setParameter("cropId", cropId)
                .getResultList();

        return entities.stream().map(SensorDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Asocia un sensor a un cultivo usando el procedimiento almacenado.
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
     * Desasocia un sensor de un cultivo usando el procedimiento almacenado.
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
}