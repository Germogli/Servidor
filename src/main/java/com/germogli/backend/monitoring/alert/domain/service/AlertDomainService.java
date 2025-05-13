package com.germogli.backend.monitoring.alert.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.monitoring.alert.application.dto.AlertResponseDTO;
import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;
import com.germogli.backend.monitoring.alert.domain.repository.AlertDomainRepository;
import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.repository.CropDomainRepository;
import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;
import com.germogli.backend.monitoring.sensor.domain.repository.SensorDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de alertas.
 * Contiene la lógica de negocio para operaciones con alertas.
 */
@Service
@RequiredArgsConstructor
public class AlertDomainService {

    private final AlertDomainRepository alertRepository;
    private final CropDomainRepository cropRepository;
    private final SensorDomainRepository sensorRepository;
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Obtiene una alerta por su ID.
     * Verifica que el usuario tenga acceso al cultivo asociado a la alerta.
     *
     * @param id ID de la alerta.
     * @return La alerta encontrada.
     * @throws ResourceNotFoundException si la alerta no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public AlertDomain getAlertById(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        AlertDomain alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(alert.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + alert.getCropId()));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a esta alerta");
        }

        return alert;
    }

    /**
     * Obtiene todas las alertas de un cultivo específico.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @return Lista de alertas del cultivo.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public List<AlertDomain> getAlertsByCropId(Integer cropId) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a las alertas de este cultivo");
        }

        return alertRepository.findByCropId(cropId);
    }

    /**
     * Obtiene todas las alertas de un nivel específico para los cultivos del usuario.
     * Los administradores pueden ver alertas de todos los cultivos.
     *
     * @param alertLevel Nivel de alerta (low, medium, high, critical).
     * @return Lista de alertas del nivel especificado.
     */
    public List<AlertDomain> getAlertsByLevel(String alertLevel) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        List<AlertDomain> alerts = alertRepository.findByAlertLevel(alertLevel);

        // Si no es administrador, filtrar las alertas para mostrar solo las de sus cultivos
        if (!isAdmin) {
            List<CropDomain> userCrops = cropRepository.findByUserId(currentUser.getId());
            List<Integer> userCropIds = userCrops.stream()
                    .map(CropDomain::getId)
                    .collect(Collectors.toList());

            return alerts.stream()
                    .filter(alert -> userCropIds.contains(alert.getCropId()))
                    .collect(Collectors.toList());
        }

        return alerts;
    }

    /**
     * Obtiene todas las alertas de un cultivo específico en un rango de fechas.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @param startDate Fecha de inicio del rango (opcional).
     * @param endDate Fecha de fin del rango (opcional).
     * @return Lista de alertas que cumplen con los criterios.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public List<AlertDomain> getAlertsByCropIdAndDateRange(Integer cropId, LocalDateTime startDate, LocalDateTime endDate) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a las alertas de este cultivo");
        }

        // Establecer valores por defecto si no se proporcionan
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusDays(7);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        return alertRepository.findByCropIdAndDateRange(cropId, effectiveStartDate, effectiveEndDate);
    }

    /**
     * Elimina una alerta por su ID.
     * Solo los administradores pueden eliminar alertas.
     *
     * @param id ID de la alerta a eliminar.
     * @throws ResourceNotFoundException si la alerta no existe.
     * @throws AccessDeniedException si el usuario no es administrador.
     */
    @Transactional
    public void deleteAlert(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el usuario sea administrador
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("Solo los administradores pueden eliminar alertas");
        }

        // Verificar que la alerta exista
        AlertDomain alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        alertRepository.deleteById(id);
    }

    /**
     * Crea manualmente una alerta para un cultivo y sensor específicos.
     * Solo los administradores pueden crear alertas manualmente.
     *
     * @param cropId ID del cultivo.
     * @param sensorId ID del sensor.
     * @param alertLevel Nivel de alerta.
     * @param alertMessage Mensaje de alerta.
     * @return La alerta creada.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no es administrador.
     */
    @Transactional
    public AlertDomain createAlert(Integer cropId, Integer sensorId, String alertLevel, String alertMessage) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el usuario sea administrador
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("Solo los administradores pueden crear alertas manualmente");
        }

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + sensorId));

        // Crear la alerta
        AlertDomain alert = AlertDomain.builder()
                .cropId(cropId)
                .sensorId(sensorId)
                .alertLevel(alertLevel)
                .alertMessage(alertMessage)
                .alertDatetime(LocalDateTime.now())
                .build();

        AlertDomain savedAlert = alertRepository.save(alert);

        // Enviar notificación al propietario del cultivo
        notificationService.sendNotification(
                crop.getUserId(),
                alertMessage,
                "sensor_alert"
        );

        return savedAlert;
    }

    /**
     * Convierte un objeto AlertDomain en un DTO de respuesta.
     * Incluye información adicional del cultivo y el sensor.
     *
     * @param alert Alerta a convertir.
     * @return DTO con la información de la alerta.
     */
    public AlertResponseDTO toResponse(AlertDomain alert) {
        // Obtener información adicional del cultivo y sensor
        CropDomain crop = cropRepository.findById(alert.getCropId())
                .orElse(null);

        SensorDomain sensor = null;
        if (alert.getSensorId() != null) {
            sensor = sensorRepository.findById(alert.getSensorId())
                    .orElse(null);
        }

        return AlertResponseDTO.builder()
                .id(alert.getId())
                .cropId(alert.getCropId())
                .sensorId(alert.getSensorId())
                .alertMessage(alert.getAlertMessage())
                .alertLevel(alert.getAlertLevel())
                .alertDatetime(alert.getAlertDatetime())
                .cropName(crop != null ? crop.getCropName() : null)
                .sensorType(sensor != null ? sensor.getSensorType() : null)
                .build();
    }

    /**
     * Convierte una lista de AlertDomain en una lista de DTOs de respuesta.
     *
     * @param alerts Lista de alertas.
     * @return Lista de DTOs.
     */
    public List<AlertResponseDTO> toResponseList(List<AlertDomain> alerts) {
        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}