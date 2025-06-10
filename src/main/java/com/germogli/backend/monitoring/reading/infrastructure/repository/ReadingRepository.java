package com.germogli.backend.monitoring.reading.infrastructure.repository;

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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de ReadingDomainRepository utilizando procedimientos almacenados.
 */
@Repository
@RequiredArgsConstructor
public class ReadingRepository implements ReadingDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda una lectura de sensor utilizando sp_create_sensor_reading.
     */
    @Override
    @Transactional
    public ReadingDomain save(ReadingDomain reading) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_sensor_reading");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_reading_value", BigDecimal.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_reading_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_reading_id", Integer.class, ParameterMode.OUT);

        // Formatear la fecha para MySQL
        String formattedDate = reading.getReadingDate() != null ?
                reading.getReadingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        query.setParameter("p_crop_id", reading.getCropId());
        query.setParameter("p_sensor_id", reading.getSensorId());
        query.setParameter("p_reading_value", reading.getReadingValue());
        query.setParameter("p_reading_date", formattedDate);

        query.execute();

        // Obtener el ID generado
        Integer readingId = (Integer) query.getOutputParameterValue("p_reading_id");
        reading.setId(readingId);

        return reading;
    }

    /**
     * Busca una lectura por su ID utilizando sp_get_reading_by_id.
     */
    @Override
    public Optional<ReadingDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_reading_by_id", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_reading_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reading_id", id);
        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ReadingDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todas las lecturas utilizando sp_get_all_readings.
     */
    @Override
    public List<ReadingDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_readings", ReadingEntity.class);
        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        return resultList.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una lectura por su ID utilizando sp_delete_reading.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_reading");
        query.registerStoredProcedureParameter("p_reading_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reading_id", id);
        query.execute();
    }

    /**
     * Encuentra todas las lecturas de un cultivo específico utilizando sp_get_readings_by_crop_id.
     */
    @Override
    public List<ReadingDomain> findByCropId(Integer cropId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_readings_by_crop_id", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        return resultList.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un sensor específico utilizando sp_get_readings_by_sensor_id.
     */
    @Override
    public List<ReadingDomain> findBySensorId(Integer sensorId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_readings_by_sensor_id", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_sensor_id", sensorId);
        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        return resultList.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos utilizando sp_get_readings_by_crop_and_sensor.
     */
    @Override
    public List<ReadingDomain> findByCropIdAndSensorId(Integer cropId, Integer sensorId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_readings_by_crop_and_sensor", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        return resultList.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las lecturas de un cultivo y sensor específicos en un rango de fechas
     * utilizando sp_get_readings_history.
     */
    @Override
    public List<ReadingDomain> findByCropIdAndSensorIdAndDateRange(
            Integer cropId, Integer sensorId, LocalDateTime startDate, LocalDateTime endDate, Integer limit) {

        // Formatear las fechas para MySQL
        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_readings_history", ReadingEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_start_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_end_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_start_date", formattedStartDate);
        query.setParameter("p_end_date", formattedEndDate);
        query.setParameter("p_limit", limit);

        query.execute();

        List<ReadingEntity> resultList = query.getResultList();
        return resultList.stream().map(ReadingDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Guarda un lote de lecturas utilizando sp_batch_insert_sensor_readings.
     */
    @Override
    @Transactional
    public List<ReadingDomain> saveAll(List<ReadingDomain> readings) {
        List<ReadingDomain> savedReadings = new ArrayList<>();

        for (ReadingDomain reading : readings) {
            // Usamos el procedimiento para insertar individualmente cada lectura
            // ya que el procedimiento almacenado sp_batch_insert_sensor_readings no está implementado
            // en la base de datos aún
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_batch_insert_reading");
            query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_reading_value", BigDecimal.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_reading_date", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_reading_id", Integer.class, ParameterMode.OUT);

            // Formatear la fecha para MySQL
            String formattedDate = reading.getReadingDate() != null ?
                    reading.getReadingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            query.setParameter("p_crop_id", reading.getCropId());
            query.setParameter("p_sensor_id", reading.getSensorId());
            query.setParameter("p_reading_value", reading.getReadingValue());
            query.setParameter("p_reading_date", formattedDate);

            query.execute();

            // Obtener el ID generado
            Integer readingId = (Integer) query.getOutputParameterValue("p_reading_id");
            reading.setId(readingId);

            savedReadings.add(reading);
        }

        return savedReadings;
    }
}