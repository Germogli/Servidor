package com.germogli.backend.monitoring.reading.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.germogli.backend.monitoring.reading.domain.model.ReadingDomain;
import com.germogli.backend.monitoring.reading.domain.repository.ReadingDomainRepository;
import com.germogli.backend.monitoring.reading.infrastructure.entity.ReadingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de ReadingDomainRepository utilizando procedimientos almacenados
 * y consultas JPA.
 */
@Repository
@RequiredArgsConstructor
public class ReadingRepository implements ReadingDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    /**
     * Guarda una lectura de sensor utilizando el procedimiento almacenado.
     */
    @Override
    @Transactional
    public ReadingDomain save(ReadingDomain reading) {
        // Crear lectura de sensor usando sp_create_sensor_reading
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_sensor_reading");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_reading_value", BigDecimal.class, ParameterMode.IN);

        query.setParameter("p_crop_id", reading.getCropId());
        query.setParameter("p_sensor_id", reading.getSensorId());
        query.setParameter("p_reading_value", reading.getReadingValue());

        query.execute();

        // Obtener el último ID insertado
        Integer lastInsertId = (Integer) entityManager.createNativeQuery(
                        "SELECT LAST_INSERT_ID()")
                .getSingleResult();

        reading.setId(lastInsertId);

        // Si no se proporcionó una fecha, establecer la fecha actual
        if (reading.getReadingDate() == null) {
            reading.setReadingDate(LocalDateTime.now());
        }

        return reading;
    }

    /**
     * Busca una lectura por su ID.
     */
    @Override
    public Optional<ReadingDomain> findById(Integer id) {
        ReadingEntity entity = entityManager.find(ReadingEntity.class, id);
        return Optional.ofNullable(entity).map(ReadingDomain::fromEntityStatic);
    }

    /**
     * Obtiene todas las lecturas.
     */
    @Override
    public List<ReadingDomain> findAll() {
        List<ReadingEntity> entities = entityManager.createQuery(
                        "SELECT r FROM ReadingEntity r", ReadingEntity.class)
                .getResultList();
        return entities.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una lectura por su ID.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        entityManager.createQuery("DELETE FROM ReadingEntity r WHERE r.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Encuentra todas las lecturas de un cultivo específico.
     */
    @Override
    public List<ReadingDomain> findByCropId(Integer cropId) {
        List<ReadingEntity> entities = entityManager.createQuery(
                        "SELECT r FROM ReadingEntity r WHERE r.cropId = :cropId ORDER BY r.readingDate DESC",
                        ReadingEntity.class)
                .setParameter("cropId", cropId)
                .getResultList();
        return entities.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un sensor específico.
     */
    @Override
    public List<ReadingDomain> findBySensorId(Integer sensorId) {
        List<ReadingEntity> entities = entityManager.createQuery(
                        "SELECT r FROM ReadingEntity r WHERE r.sensorId = :sensorId ORDER BY r.readingDate DESC",
                        ReadingEntity.class)
                .setParameter("sensorId", sensorId)
                .getResultList();
        return entities.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos.
     */
    @Override
    public List<ReadingDomain> findByCropIdAndSensorId(Integer cropId, Integer sensorId) {
        List<ReadingEntity> entities = entityManager.createQuery(
                        "SELECT r FROM ReadingEntity r WHERE r.cropId = :cropId AND r.sensorId = :sensorId " +
                                "ORDER BY r.readingDate DESC",
                        ReadingEntity.class)
                .setParameter("cropId", cropId)
                .setParameter("sensorId", sensorId)
                .getResultList();
        return entities.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos en un rango de fechas.
     * Utiliza el procedimiento almacenado sp_get_readings_history.
     */
    @Override
    public List<ReadingDomain> findByCropIdAndSensorIdAndDateRange(
            Integer cropId, Integer sensorId, LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_readings_history", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_start_date", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_end_date", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_start_date", startDate);
        query.setParameter("p_end_date", endDate);
        query.setParameter("p_limit", limit);

        query.execute();

        @SuppressWarnings("unchecked")
        List<ReadingEntity> entities = query.getResultList();
        return entities.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Guarda un lote de lecturas utilizando el procedimiento almacenado sp_batch_insert_sensor_readings.
     * Convierte la lista de lecturas a formato JSON para pasarla al procedimiento.
     */
    @Override
    @Transactional
    public List<ReadingDomain> saveAll(List<ReadingDomain> readings) {
        try {
            // Convertir la lista de lecturas a formato JSON
            String readingsJson = objectMapper.writeValueAsString(readings);

            // Llamar al procedimiento almacenado para inserción por lotes
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_batch_insert_sensor_readings");
            query.registerStoredProcedureParameter("p_readings", String.class, ParameterMode.IN);
            query.setParameter("p_readings", readingsJson);

            query.execute();

            // Como no podemos obtener los IDs generados directamente, establecemos fechas
            // para las lecturas que no las tengan
            for (ReadingDomain reading : readings) {
                if (reading.getReadingDate() == null) {
                    reading.setReadingDate(LocalDateTime.now());
                }
            }

            return readings;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir lecturas a JSON", e);
        }
    }
}