package com.germogli.backend.community.group.domain.service;

import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.exception.RoleNotAllowedException;
import com.germogli.backend.common.notification.NotificationPublisher;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.group.domain.model.GroupDomain;
import com.germogli.backend.community.group.domain.repository.GroupDomainRepository;
import com.germogli.backend.community.group.application.dto.CreateGroupRequestDTO;
import com.germogli.backend.community.group.application.dto.GroupResponseDTO;
import com.germogli.backend.community.group.application.dto.UpdateGroupRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de grupos.
 * Contiene la lógica para crear, obtener, listar, actualizar y eliminar grupos.
 */
@Service
@RequiredArgsConstructor
public class GroupDomainService {

    // Repositorio de grupos, inyectado para realizar operaciones de persistencia.
    private final GroupDomainRepository groupRepository;
    // Servicio para enviar notificaciones.
    private final NotificationPublisher notificationPublisher;
    // Servicio compartido para obtener el usuario autenticado y verificar roles.
    private final CommunitySharedService sharedService;

    /**
     * Verifica que el usuario autenticado tenga al menos uno de los roles permitidos.
     *
     * @param rolesAllowed Roles permitidos.
     * @throws RoleNotAllowedException si el usuario no posee ninguno de los roles.
     */
    private void verifyRole(String... rolesAllowed) {
        var currentUser = sharedService.getAuthenticatedUser();
        boolean allowed = false;
        for (String role : rolesAllowed) {
            if (sharedService.hasRole(currentUser, role)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new RoleNotAllowedException("No tienes permisos para realizar esta acción.");
        }
    }

    /**
     * Crea un nuevo grupo.
     * Requiere que el usuario tenga rol ADMINISTRADOR o MODERADOR.
     *
     * @param request DTO con los datos para crear el grupo.
     * @return Grupo creado.
     */
    public GroupDomain createGroup(CreateGroupRequestDTO request) {
        verifyRole("ADMINISTRADOR", "MODERADOR");
        GroupDomain group = GroupDomain.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        GroupDomain savedGroup = groupRepository.save(group);
        notificationPublisher.publishNotification(null,
                "Se ha creado un nuevo grupo: " + request.getName(),
                "group");
        return savedGroup;
    }

    /**
     * Obtiene un grupo por su ID.
     *
     * @param id Identificador del grupo.
     * @return Grupo encontrado.
     * @throws ResourceNotFoundException si el grupo no existe.
     */
    public GroupDomain getGroupById(Integer id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id: " + id));
    }

    /**
     * Obtiene la lista de todos los grupos.
     *
     * @return Lista de grupos.
     * @throws ResourceNotFoundException si no hay grupos disponibles.
     */
    public List<GroupDomain> getAllGroups() {
        List<GroupDomain> groups = groupRepository.findAll();
        if (groups.isEmpty()) {
            throw new ResourceNotFoundException("No hay grupos disponibles.");
        }
        return groups;
    }

    /**
     * Actualiza la información de un grupo.
     * Requiere que el usuario tenga rol ADMINISTRADOR o MODERADOR.
     *
     * @param id      Identificador del grupo a actualizar.
     * @param request DTO con los datos a actualizar.
     * @return Grupo actualizado.
     */
    public GroupDomain updateGroup(Integer id, UpdateGroupRequestDTO request) {
        verifyRole("ADMINISTRADOR", "MODERADOR");
        GroupDomain existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id: " + id));
        existingGroup.setName(request.getName());
        existingGroup.setDescription(request.getDescription());
        GroupDomain updatedGroup = groupRepository.save(existingGroup);
        notificationPublisher.publishNotification(null,
                "El grupo " + request.getName() + " ha sido actualizado",
                "group");
        return updatedGroup;
    }

    /**
     * Elimina un grupo.
     * Solo los usuarios con rol ADMINISTRADOR pueden realizar esta acción.
     *
     * @param id Identificador del grupo a eliminar.
     * @throws ResourceNotFoundException si el grupo no existe.
     */
    public void deleteGroup(Integer id) {
        verifyRole("ADMINISTRADOR");
        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grupo no encontrado con id: " + id);
        }
        groupRepository.deleteById(id);
        notificationPublisher.publishNotification(null,
                "Se ha eliminado un grupo",
                "group");
    }

    /**
     * Convierte un objeto GroupDomain en un DTO de respuesta.
     *
     * @param group Grupo a convertir.
     * @return DTO con la información del grupo.
     */
    public GroupResponseDTO toResponse(GroupDomain group) {
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .creationDate(group.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de GroupDomain en una lista de DTOs de respuesta.
     *
     * @param groups Lista de grupos.
     * @return Lista de DTOs.
     */
    public List<GroupResponseDTO> toResponseList(List<GroupDomain> groups) {
        return groups.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Verifica la existencia de un grupo.
     *
     * @param id Identificador del grupo.
     * @return true si existe, false en caso contrario.
     */
    public boolean existsById(Integer id) {
        return groupRepository.existsById(id);
    }
}
