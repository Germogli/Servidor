package com.germogli.backend.monitoring.alert.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.monitoring.domain.service.MonitoringSharedService;
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

import java.util.ArrayList;
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
    private final MonitoringSharedService sharedService;
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
     * Obtiene todas las alertas de los cultivos del usuario autenticado.
     *
     * @return Lista de alertas del usuario.
     */
    public List<AlertDomain> getUserAlerts() {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Obtener todos los cultivos del usuario
        List<CropDomain> userCrops = cropRepository.findByUserId(currentUser.getId());

        // Si no tiene cultivos, devolver lista vacía
        if (userCrops.isEmpty()) {
            return new ArrayList<>();
        }

        // Extraer los IDs de cultivos
        List<Integer> cropIds = userCrops.stream()
                .map(CropDomain::getId)
                .collect(Collectors.toList());

        // Obtener todas las alertas de esos cultivos
        List<AlertDomain> userAlerts = new ArrayList<>();
        for (Integer cropId : cropIds) {
            userAlerts.addAll(alertRepository.findByCropId(cropId));
        }

        return userAlerts;
    }

    /**
     * Elimina una alerta por su ID.
     * Verifica que el usuario tenga acceso al cultivo asociado a la alerta.
     *
     * @param id ID de la alerta a eliminar.
     * @throws ResourceNotFoundException si la alerta no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void deleteAlert(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Obtener la alerta
        AlertDomain alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        // Obtener el cultivo para verificar el propietario
        CropDomain crop = cropRepository.findById(alert.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + alert.getCropId()));

        // Verificar que el usuario actual sea el propietario del cultivo
        boolean isOwner = crop.getUserId().equals(currentUser.getId());

        if (!isOwner) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta alerta");
        }

        alertRepository.deleteById(id);
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