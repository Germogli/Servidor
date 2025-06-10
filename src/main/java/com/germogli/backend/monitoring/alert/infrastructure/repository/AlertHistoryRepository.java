package com.germogli.backend.monitoring.alert.infrastructure.repository;

import com.germogli.backend.monitoring.alert.domain.model.AlertHistoryDomain;
import com.germogli.backend.monitoring.alert.domain.repository.AlertHistoryDomainRepository;
import com.germogli.backend.monitoring.alert.infrastructure.entity.AlertHistoryEntity;
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
 * Implementación de AlertHistoryDomainRepository utilizando procedimientos almacenados.
 */
@Repository
@RequiredArgsConstructor
public class AlertHistoryRepository implements AlertHistoryDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda un registro de historial de alerta utilizando sp_create_alert_history.
     */
    @Override
    @Transactional
    public AlertHistoryDomain save(AlertHistoryDomain alertHistory) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_alert_history");
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_type", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_message", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_status", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_resolution_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_comments", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_sensor_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.OUT);

        // Formatear la fecha de resolución
        String formattedResolutionDate = alertHistory.getResolutionDate() != null ?
                alertHistory.getResolutionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                null;

        query.setParameter("p_user_id", alertHistory.getUserId());
        query.setParameter("p_alert_type", alertHistory.getAlertType());
        query.setParameter("p_message", alertHistory.getMessage());
        query.setParameter("p_alert_level", alertHistory.getAlertLevel());
        query.setParameter("p_alert_status", alertHistory.getAlertStatus());
        query.setParameter("p_resolution_date", formattedResolutionDate);
        query.setParameter("p_comments", alertHistory.getComments());
        query.setParameter("p_crop_id", alertHistory.getCropId());
        query.setParameter("p_sensor_id", alertHistory.getSensorId());

        query.execute();

        // Obtener el ID generado
        Integer alertId = (Integer) query.getOutputParameterValue("p_alert_id");
        alertHistory.setId(alertId);

        return alertHistory;
    }

    /**
     * Busca un registro de historial de alerta por su ID utilizando sp_get_alert_history_by_id.
     */
    @Override
    public Optional<AlertHistoryDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alert_history_by_id", AlertHistoryEntity.class);
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_alert_id", id);
        query.execute();

        List<AlertHistoryEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AlertHistoryDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todos los registros de historial de alertas utilizando sp_get_all_alert_history.
     */
    @Override
    public List<AlertHistoryDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_all_alert_history", AlertHistoryEntity.class);
        query.execute();

        List<AlertHistoryEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertHistoryDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un registro de historial de alerta por su ID utilizando sp_delete_alert_history.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_alert_history");
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_alert_id", id);
        query.execute();
    }

    /**
     * Encuentra todos los registros de historial de alertas de un cultivo específico
     * utilizando sp_get_alert_history_by_crop_id.
     */
    @Override
    public List<AlertHistoryDomain> findByCropId(Integer cropId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alert_history_by_crop_id", AlertHistoryEntity.class);
        query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_crop_id", cropId);
        query.execute();

        List<AlertHistoryEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertHistoryDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todos los registros de historial de alertas de un nivel específico
     * utilizando sp_get_alert_history_by_level.
     */
    @Override
    public List<AlertHistoryDomain> findByAlertLevel(String alertLevel) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alert_history_by_level", AlertHistoryEntity.class);
        query.registerStoredProcedureParameter("p_alert_level", String.class, ParameterMode.IN);
        query.setParameter("p_alert_level", alertLevel);
        query.execute();

        List<AlertHistoryEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertHistoryDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Encuentra todos los registros de historial de alertas de un usuario específico
     * utilizando sp_get_alert_history_by_user_id.
     */
    @Override
    public List<AlertHistoryDomain> findByUserId(Integer userId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "sp_get_alert_history_by_user_id", AlertHistoryEntity.class);
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_user_id", userId);
        query.execute();

        List<AlertHistoryEntity> resultList = query.getResultList();
        return resultList.stream().map(AlertHistoryDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de un registro de historial de alerta utilizando sp_update_alert_history_status.
     */
    @Override
    @Transactional
    public AlertHistoryDomain updateStatus(Integer id, String status, LocalDateTime resolutionDate, String comments) {
        // Formatear la fecha de resolución
        String formattedResolutionDate = resolutionDate != null ?
                resolutionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                null;

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_alert_history_status");
        query.registerStoredProcedureParameter("p_alert_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_alert_status", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_resolution_date", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_comments", String.class, ParameterMode.IN);

        query.setParameter("p_alert_id", id);
        query.setParameter("p_alert_status", status);
        query.setParameter("p_resolution_date", formattedResolutionDate);
        query.setParameter("p_comments", comments);

        query.execute();

        return findById(id).orElseThrow(() ->
                new RuntimeException("Error al actualizar el estado del historial de alerta"));
    }
}