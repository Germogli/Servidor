package com.germogli.backend.monitoring.alert.infrastructure.repository;

import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;
import com.germogli.backend.monitoring.alert.domain.repository.AlertDomainRepository;
import com.germogli.backend.monitoring.alert.infrastructure.entity.AlertEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de AlertDomainRepository utilizando procedimientos almacenados
 * y consultas JPA.
 */
@Repository
@RequiredArgsConstructor
public class AlertRepository implements AlertDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda una alerta utilizando el procedimiento almacenado sp_create_crop_alert.
     */
    @Override
    @Transactional
    public AlertDomain save(AlertDomain alert) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_crop_alert");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_message", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);

        query.setParameter("p_crop_id", alert.getCropId());
        query.setParameter("p_sensor_id", alert.getSensorId());
        query.setParameter("p_alert_message", alert.getAlertMessage());
        query.setParameter("p_alert_level", alert.getAlertLevel());

        query.execute();

        // Obtener el último ID insertado
        Integer lastInsertId = (Integer) entityManager.createNativeQuery(
                        "SELECT LAST_INSERT_ID()")
                .getSingleResult();

        alert.setId(lastInsertId);

        // Si no se proporcionó una fecha, establecer la fecha actual
        if (alert.getAlertDatetime() == null) {
            alert.setAlertDatetime(LocalDateTime.now());
        }

        return alert;
    }

    /**
     * Busca una alerta por su ID.
     */
    @Override
    public Optional<AlertDomain> findById(Integer id) {
        AlertEntity entity = entityManager.find(AlertEntity.class, id);
        return Optional.ofNullable(entity).map(AlertDomain::fromEntityStatic);
    }

    /**
     * Obtiene todas las alertas.
     */
    @Override
    public List<AlertDomain> findAll() {
        List<AlertEntity> entities = entityManager.createQuery(
                        "SELECT a FROM AlertEntity a ORDER BY a.alertDatetime DESC",
                        AlertEntity.class)
                .getResultList();
        return entities.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una alerta por su ID.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        entityManager.createQuery("DELETE FROM AlertEntity a WHERE a.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Encuentra todas las alertas de un cultivo específico.
     */
    @Override
    public List<AlertDomain> findByCropId(Integer cropId) {
        List<AlertEntity> entities = entityManager.createQuery(
                        "SELECT a FROM AlertEntity a WHERE a.cropId = :cropId ORDER BY a.alertDatetime DESC",
                        AlertEntity.class)
                .setParameter("cropId", cropId)
                .getResultList();
        return entities.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las alertas de un nivel específico.
     */
    @Override
    public List<AlertDomain> findByAlertLevel(String alertLevel) {
        List<AlertEntity> entities = entityManager.createQuery(
                        "SELECT a FROM AlertEntity a WHERE a.alertLevel = :alertLevel ORDER BY a.alertDatetime DESC",
                        AlertEntity.class)
                .setParameter("alertLevel", alertLevel)
                .getResultList();
        return entities.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las alertas de un cultivo específico en un rango de fechas.
     */
    @Override
    public List<AlertDomain> findByCropIdAndDateRange(Integer cropId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AlertEntity> entities = entityManager.createQuery(
                        "SELECT a FROM AlertEntity a WHERE a.cropId = :cropId " +
                                "AND a.alertDatetime >= :startDate AND a.alertDatetime <= :endDate " +
                                "ORDER BY a.alertDatetime DESC",
                        AlertEntity.class)
                .setParameter("cropId", cropId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        return entities.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Procesa una alerta según umbrales predefinidos y genera el historial correspondiente
     * utilizando el procedimiento almacenado sp_process_alert.
     */
    @Override
    @Transactional
    public AlertDomain processAlert(Integer cropId, Integer sensorId, String alertLevel, String alertMessage) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_process_alert");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_message", String.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_alert_level", alertLevel);
        query.setParameter("p_alert_message", alertMessage);

        query.execute();

        // Construir y retornar el dominio de alerta
        return AlertDomain.builder()
                .cropId(cropId)
                .sensorId(sensorId)
                .alertLevel(alertLevel)
                .alertMessage(alertMessage)
                .alertDatetime(LocalDateTime.now())
                .build();
    }
}