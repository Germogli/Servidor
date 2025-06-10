package com.germogli.backend.monitoring.crop.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.monitoring.crop.application.dto.CropRequestDTO;
import com.germogli.backend.monitoring.crop.application.dto.CropResponseDTO;
import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.repository.CropDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de cultivos.
 * Contiene la lógica de negocio para operaciones con cultivos.
 */
@Service
@RequiredArgsConstructor
public class CropDomainService {

    private final CropDomainRepository cropRepository;
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea un nuevo cultivo para el usuario autenticado.
     *
     * @param request DTO con los datos del cultivo.
     * @return El cultivo creado.
     */
    @Transactional
    public CropDomain createCrop(CropRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        CropDomain crop = CropDomain.builder()
                .userId(currentUser.getId())
                .cropName(request.getCropName())
                .cropType(request.getCropType())
                .startDate(LocalDateTime.now())
                .build();

        CropDomain savedCrop = cropRepository.save(crop);

        // Enviar notificación al usuario
        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha creado el cultivo: " + request.getCropName(),
                "crop"
        );

        return savedCrop;
    }

    /**
     * Obtiene un cultivo por su ID.
     * Verifica que el usuario actual tenga acceso al cultivo.
     *
     * @param id ID del cultivo.
     * @return El cultivo encontrado.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public CropDomain getCropById(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        CropDomain crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + id));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a este cultivo");
        }

        return crop;
    }

    /**
     * Obtiene todos los cultivos del usuario autenticado.
     * Si el usuario es administrador, puede obtener todos los cultivos.
     *
     * @return Lista de cultivos.
     */
    public List<CropDomain> getUserCrops() {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (isAdmin) {
            return cropRepository.findAll();
        } else {
            return cropRepository.findByUserId(currentUser.getId());
        }
    }

    /**
     * Actualiza un cultivo existente.
     * Verifica que el usuario actual tenga acceso al cultivo.
     *
     * @param id ID del cultivo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return El cultivo actualizado.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public CropDomain updateCrop(Integer id, CropRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        CropDomain existingCrop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + id));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = existingCrop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar este cultivo");
        }

        // Actualizar los campos
        existingCrop.setCropName(request.getCropName());
        existingCrop.setCropType(request.getCropType());

        CropDomain updatedCrop = cropRepository.save(existingCrop);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                existingCrop.getUserId(),
                "El cultivo " + request.getCropName() + " ha sido actualizado",
                "crop"
        );

        return updatedCrop;
    }

    /**
     * Elimina un cultivo.
     * Verifica que el usuario actual tenga acceso al cultivo.
     *
     * @param id ID del cultivo a eliminar.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public void deleteCrop(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        CropDomain existingCrop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + id));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = existingCrop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar este cultivo");
        }

        cropRepository.deleteById(id);

        // Enviar notificación al propietario
        notificationService.sendNotification(
                existingCrop.getUserId(),
                "El cultivo " + existingCrop.getCropName() + " ha sido eliminado",
                "crop"
        );
    }

    /**
     * Convierte un objeto CropDomain en un DTO de respuesta.
     *
     * @param crop Cultivo a convertir.
     * @return DTO con la información del cultivo.
     */
    public CropResponseDTO toResponse(CropDomain crop) {
        return CropResponseDTO.builder()
                .id(crop.getId())
                .userId(crop.getUserId())
                .cropName(crop.getCropName())
                .cropType(crop.getCropType())
                .startDate(crop.getStartDate())
                .build();
    }

    /**
     * Convierte una lista de CropDomain en una lista de DTOs de respuesta.
     *
     * @param crops Lista de cultivos.
     * @return Lista de DTOs.
     */
    public List<CropResponseDTO> toResponseList(List<CropDomain> crops) {
        return crops.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}