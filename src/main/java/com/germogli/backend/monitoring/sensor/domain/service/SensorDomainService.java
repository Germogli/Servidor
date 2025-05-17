package com.germogli.backend.monitoring.sensor.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.monitoring.domain.service.MonitoringSharedService;
import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.repository.CropDomainRepository;
import com.germogli.backend.monitoring.sensor.application.dto.SensorRequestDTO;
import com.germogli.backend.monitoring.sensor.application.dto.SensorResponseDTO;
import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;
import com.germogli.backend.monitoring.sensor.domain.repository.SensorDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de sensores.
 * Contiene la lógica de negocio para operaciones con sensores.
 */
@Service
@RequiredArgsConstructor
public class SensorDomainService {

    private final SensorDomainRepository sensorRepository;
    private final CropDomainRepository cropRepository;
    private final MonitoringSharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea un nuevo sensor.
     * No requiere permisos especiales, cualquier usuario puede crear sensores.
     *
     * @param request DTO con los datos del sensor.
     * @return El sensor creado.
     */
    @Transactional
    public SensorDomain createSensor(SensorRequestDTO request) {
        // Cualquier usuario puede crear sensores, no se verifica rol

        SensorDomain sensor = SensorDomain.builder()
                .sensorType(request.getSensorType())
                .unitOfMeasurement(request.getUnitOfMeasurement())
                .build();

        return sensorRepository.save(sensor);
    }

    /**
     * Obtiene un sensor por su ID.
     *
     * @param id ID del sensor.
     * @return El sensor encontrado.
     * @throws ResourceNotFoundException si el sensor no existe.
     */
    public SensorDomain getSensorById(Integer id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + id));
    }

    /**
     * Obtiene todos los sensores disponibles.
     *
     * @return Lista de sensores.
     */
    public List<SensorDomain> getAllSensors() {
        return sensorRepository.findAll();
    }

    /**
     * Obtiene todos los sensores asociados a los cultivos del usuario autenticado.
     *
     * @return Lista de sensores del usuario.
     */
    public List<SensorDomain> getUserSensors() {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Obtener todos los cultivos del usuario
        List<CropDomain> userCrops = cropRepository.findByUserId(currentUser.getId());

        // Si no tiene cultivos, devolver lista vacía
        if (userCrops.isEmpty()) {
            return new ArrayList<>();
        }

        // Obtener todos los sensores asociados a sus cultivos
        Set<SensorDomain> userSensors = new HashSet<>();
        for (CropDomain crop : userCrops) {
            userSensors.addAll(sensorRepository.findByCropId(crop.getId()));
        }

        return new ArrayList<>(userSensors);
    }

    /**
     * Actualiza un sensor existente.
     * No requiere permisos especiales, cualquier usuario puede actualizar sensores.
     *
     * @param id ID del sensor a actualizar.
     * @param request DTO con los nuevos datos.
     * @return El sensor actualizado.
     * @throws ResourceNotFoundException si el sensor no existe.
     */
    @Transactional
    public SensorDomain updateSensor(Integer id, SensorRequestDTO request) {
        // Cualquier usuario puede actualizar sensores, no se verifica rol

        SensorDomain existingSensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + id));

        // Actualizar los campos
        existingSensor.setSensorType(request.getSensorType());
        existingSensor.setUnitOfMeasurement(request.getUnitOfMeasurement());

        return sensorRepository.save(existingSensor);
    }

    /**
     * Elimina un sensor.
     * No requiere permisos especiales, cualquier usuario puede eliminar sensores.
     *
     * @param id ID del sensor a eliminar.
     * @throws ResourceNotFoundException si el sensor no existe.
     */
    @Transactional
    public void deleteSensor(Integer id) {
        // Cualquier usuario puede eliminar sensores, no se verifica rol

        // Verificar que el sensor exista
        SensorDomain existingSensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + id));

        sensorRepository.deleteById(id);
    }

    /**
     * Obtiene todos los sensores asociados a un cultivo.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @return Lista de sensores asociados al cultivo.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public List<SensorDomain> getSensorsByCropId(Integer cropId) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a los sensores de este cultivo");
        }

        return sensorRepository.findByCropId(cropId);
    }

    /**
     * Asocia un sensor a un cultivo con umbrales personalizados.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Umbral mínimo.
     * @param maxThreshold Umbral máximo.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void addSensorToCropWithThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar los sensores de este cultivo");
        }

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + sensorId));

        sensorRepository.addSensorToCropWithThresholds(cropId, sensorId, minThreshold, maxThreshold);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se ha añadido un sensor de " + sensor.getSensorType() + " a tu cultivo " + crop.getCropName(),
                "sensor"
        );
    }

    /**
     * Asocia un sensor a un cultivo.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @param sensorId ID del sensor.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void addSensorToCrop(Integer cropId, Integer sensorId) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar los sensores de este cultivo");
        }

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + sensorId));

        sensorRepository.addSensorToCrop(cropId, sensorId);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se ha añadido un sensor de " + sensor.getSensorType() + " a tu cultivo " + crop.getCropName(),
                "sensor"
        );
    }

    /**
     * Desasocia un sensor de un cultivo.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @param sensorId ID del sensor.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void removeSensorFromCrop(Integer cropId, Integer sensorId) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar los sensores de este cultivo");
        }

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + sensorId));

        sensorRepository.removeSensorFromCrop(cropId, sensorId);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se ha eliminado un sensor de " + sensor.getSensorType() + " de tu cultivo " + crop.getCropName(),
                "sensor"
        );
    }

    /**
     * Actualiza los umbrales de un sensor asociado a un cultivo.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Nuevo umbral mínimo.
     * @param maxThreshold Nuevo umbral máximo.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void updateSensorThresholds(Integer cropId, Integer sensorId, BigDecimal minThreshold, BigDecimal maxThreshold) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar los sensores de este cultivo");
        }

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + sensorId));

        sensorRepository.updateSensorThresholds(cropId, sensorId, minThreshold, maxThreshold);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se han actualizado los umbrales del sensor de " + sensor.getSensorType() + " en tu cultivo " + crop.getCropName(),
                "sensor"
        );
    }

    /**
     * Convierte un objeto SensorDomain en un DTO de respuesta.
     *
     * @param sensor Sensor a convertir.
     * @return DTO con la información del sensor.
     */
    public SensorResponseDTO toResponse(SensorDomain sensor) {
        return SensorResponseDTO.builder()
                .id(sensor.getId())
                .sensorType(sensor.getSensorType())
                .unitOfMeasurement(sensor.getUnitOfMeasurement())
                .build();
    }

    /**
     * Convierte una lista de SensorDomain en una lista de DTOs de respuesta.
     *
     * @param sensors Lista de sensores.
     * @return Lista de DTOs.
     */
    public List<SensorResponseDTO> toResponseList(List<SensorDomain> sensors) {
        return sensors.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    /**
     * Crea un nuevo sensor y lo asocia directamente a un cultivo con umbrales.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param sensorRequest DTO con los datos del sensor
     * @param cropId ID del cultivo al que se asociará
     * @param minThreshold Umbral mínimo para las lecturas
     * @param maxThreshold Umbral máximo para las lecturas
     * @return El sensor creado y asociado
     * @throws ResourceNotFoundException si el cultivo no existe
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo
     */
    @Transactional
    public SensorDomain createSensorAndAssociateToCrop(SensorRequestDTO sensorRequest,
                                                       Integer cropId,
                                                       BigDecimal minThreshold,
                                                       BigDecimal maxThreshold) {

        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar este cultivo");
        }

        // Crear el sensor y asociarlo al cultivo en una sola operación
        SensorDomain sensor = sensorRepository.createAndAssociateToCrop(
                sensorRequest.getSensorType(),
                sensorRequest.getUnitOfMeasurement(),
                cropId,
                minThreshold,
                maxThreshold
        );

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se ha añadido un nuevo sensor de " + sensor.getSensorType() + " a tu cultivo " + crop.getCropName(),
                "sensor"
        );

        return sensor;
    }

    /**
     * Crea un nuevo sensor y lo asocia directamente a un cultivo con umbrales.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param sensorType Tipo de sensor
     * @param unitOfMeasurement Unidad de medida
     * @param cropId ID del cultivo al que se asociará
     * @param minThreshold Umbral mínimo para las lecturas
     * @param maxThreshold Umbral máximo para las lecturas
     * @return El sensor creado y asociado
     * @throws ResourceNotFoundException si el cultivo no existe
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo
     */
    @Transactional
    public SensorDomain createSensorAndAssociateToCrop(String sensorType,
                                                       String unitOfMeasurement,
                                                       Integer cropId,
                                                       BigDecimal minThreshold,
                                                       BigDecimal maxThreshold) {

        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar este cultivo");
        }

        // Crear el sensor y asociarlo al cultivo en una sola operación
        SensorDomain sensor = sensorRepository.createAndAssociateToCrop(
                sensorType,
                unitOfMeasurement,
                cropId,
                minThreshold,
                maxThreshold
        );

        // Enviar notificación al propietario
        notificationService.sendNotification(
                crop.getUserId(),
                "Se ha añadido un nuevo sensor de " + sensor.getSensorType() + " a tu cultivo " + crop.getCropName(),
                "sensor"
        );

        return sensor;
    }
}