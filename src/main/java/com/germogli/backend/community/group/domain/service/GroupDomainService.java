package com.germogli.backend.community.group.domain.service;

import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.exception.RoleNotAllowedException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.group.domain.model.GroupDomain;
import com.germogli.backend.community.group.domain.repository.GroupDomainRepository;
import com.germogli.backend.community.group.application.dto.CreateGroupRequestDTO;
import com.germogli.backend.community.group.application.dto.GroupResponseDTO;
import com.germogli.backend.community.group.application.dto.UpdateGroupRequestDTO;
import com.germogli.backend.community.group.infrastructure.crud.UserGroupCrudRepository;
import com.germogli.backend.community.group.infrastructure.entity.UserGroupEntity;
import com.germogli.backend.community.group.infrastructure.entity.UserGroupId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de grupos.
 * Contiene la lógica para crear, obtener, listar, actualizar y eliminar grupos.
 */
@Service
@RequiredArgsConstructor
public class GroupDomainService {

    private final GroupDomainRepository groupRepository;
    private final NotificationService notificationService;
    private final CommunitySharedService sharedService;
    private final UserGroupCrudRepository userGroupCrudRepository;

    /**
     * Permite al usuario autenticado unirse a un grupo.
     *
     * @param groupId ID del grupo al que se desea unir.
     */
    @Transactional
    public void joinGroup(Integer groupId) {
        // Verificar que el grupo existe
        GroupDomain group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id: " + groupId));

        // Obtener el usuario autenticado
        var currentUser = sharedService.getAuthenticatedUser();

        // Crear la clave compuesta para la relación
        UserGroupId userGroupId = UserGroupId.builder()
                .userId(currentUser.getId())
                .groupId(groupId)
                .build();

        // Si ya existe la membresía, se puede omitir la inserción
        if (userGroupCrudRepository.existsById(userGroupId)) {
            return;
        }

        // Crear y persistir la relación
        UserGroupEntity membership = UserGroupEntity.builder()
                .id(userGroupId)
                .build();

        userGroupCrudRepository.save(membership);

        // Notificar al usuario que se ha unido correctamente al grupo
        String message = "Te has unido correctamente al grupo: " + group.getName();
        notificationService.sendNotification(currentUser.getId(), message, "group");
    }

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
        notificationService.sendNotification(null,
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
        notificationService.sendNotification(null,
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
        notificationService.sendNotification(null,
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
