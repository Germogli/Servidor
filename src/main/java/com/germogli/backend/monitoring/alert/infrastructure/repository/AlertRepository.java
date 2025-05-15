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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de AlertDomainRepository utilizando procedimientos almacenados.
 */
@Repository
@RequiredArgsConstructor
public class AlertRepository implements AlertDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda una alerta utilizando sp_create_crop_alert.
     */
    @Override
    @Transactional
    public AlertDomain save(AlertDomain alert) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_crop_alert");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_message", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_crop_id", alert.getCropId());
        query.setParameter("p_sensor_id", alert.getSensorId());
        query.setParameter("p_alert_message", alert.getAlertMessage());
        query.setParameter("p_alert_level", alert.getAlertLevel());

        query.execute();

        // Obtener el ID generado
        Integer alertId = (Integer) query.getOutputParameterValue("p_alert_id");
        alert.setId(alertId);

        // Si no hay fecha de alerta, establecer la fecha actual
        if (alert.getAlertDatetime() == null) {
            alert.setAlertDatetime(LocalDateTime.now());
        }

        return alert;
    }

    /**
     * Busca una alerta por su ID utilizando sp_get_alert_by_id.
     */
    @Override
    public Optional<AlertDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_alert_by_id", AlertEntity.class);
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_alert_id", id);
        query.execute();

        List<AlertEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AlertDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todas las alertas utilizando sp_get_all_alerts.
     */
    @Override
    public List<AlertDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_alerts", AlertEntity.class);
        query.execute();

        List<AlertEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una alerta por su ID utilizando sp_delete_alert.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_alert");
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_alert_id", id);
        query.execute();
    }

    /**
     * Encuentra todas las alertas de un cultivo específico utilizando sp_get_alerts_by_crop_id.
     */
    @Override
    public List<AlertDomain> findByCropId(Integer cropId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alerts_by_crop_id", AlertEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.execute();

        List<AlertEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las alertas de un nivel específico utilizando sp_get_alerts_by_level.
     */
    @Override
    public List<AlertDomain> findByAlertLevel(String alertLevel) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alerts_by_level", AlertEntity.class);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.setParameter("p_alert_level", alertLevel);
        query.execute();

        List<AlertEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todas las alertas de un cultivo específico en un rango de fechas
     * utilizando sp_get_alerts_by_crop_id_and_date_range.
     */
    @Override
    public List<AlertDomain> findByCropIdAndDateRange(Integer cropId, LocalDateTime startDate, LocalDateTime endDate) {
        // Formatear las fechas para MySQL
        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alerts_by_crop_id_and_date_range", AlertEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_start_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_end_date", String.class, ParameterMode.IN);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_start_date", formattedStartDate);
        query.setParameter("p_end_date", formattedEndDate);

        query.execute();

        List<AlertEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Procesa una alerta según umbrales predefinidos y genera el historial correspondiente
     * utilizando sp_process_alert.
     */
    @Override
    @Transactional
    public AlertDomain processAlert(Integer cropId, Integer sensorId, String alertLevel, String alertMessage) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_process_alert");
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_message", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_crop_id", cropId);
        query.setParameter("p_sensor_id", sensorId);
        query.setParameter("p_alert_level", alertLevel);
        query.setParameter("p_alert_message", alertMessage);

        query.execute();

        // Obtener el ID generado
        Integer alertId = (Integer) query.getOutputParameterValue("p_alert_id");

        // Construir y retornar el dominio de alerta
        return AlertDomain.builder()
                .id(alertId)
                .cropId(cropId)
                .sensorId(sensorId)
                .alertLevel(alertLevel)
                .alertMessage(alertMessage)
                .alertDatetime(LocalDateTime.now())
                .build();
    }
}