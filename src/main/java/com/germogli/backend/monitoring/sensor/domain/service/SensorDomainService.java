package com.germogli.backend.monitoring.sensor.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.exception.RoleNotAllowedException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
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

import java.util.List;
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
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea un nuevo sensor.
     * Solo disponible para administradores o moderadores.
     *
     * @param request DTO con los datos del sensor.
     * @return El sensor creado.
     * @throws RoleNotAllowedException si el usuario no es administrador o moderador.
     */
    @Transactional
    public SensorDomain createSensor(SensorRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el usuario sea administrador o moderador
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        boolean isModerator = sharedService.hasRole(currentUser, "MODERADOR");

        if (!isAdmin && !isModerator) {
            throw new RoleNotAllowedException("Solo los administradores o moderadores pueden crear sensores");
        }

        SensorDomain sensor = SensorDomain.builder()
                .sensorType(request.getSensorType())
                .unitOfMeasurement(request.getUnitOfMeasurement())
                .build();

        SensorDomain savedSensor = sensorRepository.save(sensor);

        // Notificar a los administradores
        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha creado un nuevo sensor: " + request.getSensorType(),
                "sensor"
        );

        return savedSensor;
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
     * Actualiza un sensor existente.
     * Solo disponible para administradores o moderadores.
     *
     * @param id ID del sensor a actualizar.
     * @param request DTO con los nuevos datos.
     * @return El sensor actualizado.
     * @throws ResourceNotFoundException si el sensor no existe.
     * @throws RoleNotAllowedException si el usuario no es administrador o moderador.
     */
    @Transactional
    public SensorDomain updateSensor(Integer id, SensorRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el usuario sea administrador o moderador
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        boolean isModerator = sharedService.hasRole(currentUser, "MODERADOR");

        if (!isAdmin && !isModerator) {
            throw new RoleNotAllowedException("Solo los administradores o moderadores pueden actualizar sensores");
        }

        SensorDomain existingSensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + id));

        // Actualizar los campos
        existingSensor.setSensorType(request.getSensorType());
        existingSensor.setUnitOfMeasurement(request.getUnitOfMeasurement());

        return sensorRepository.save(existingSensor);
    }

    /**
     * Elimina un sensor.
     * Solo disponible para administradores.
     *
     * @param id ID del sensor a eliminar.
     * @throws ResourceNotFoundException si el sensor no existe.
     * @throws RoleNotAllowedException si el usuario no es administrador.
     */
    @Transactional
    public void deleteSensor(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el usuario sea administrador
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isAdmin) {
            throw new RoleNotAllowedException("Solo los administradores pueden eliminar sensores");
        }

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
}