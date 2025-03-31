package com.germogli.backend.community.group.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.group.application.dto.CreateGroupRequestDTO;
import com.germogli.backend.community.group.application.dto.GroupResponseDTO;
import com.germogli.backend.community.group.application.dto.UpdateGroupRequestDTO;
import com.germogli.backend.community.group.domain.model.GroupDomain;
import com.germogli.backend.community.group.domain.service.GroupDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de grupos en Community.
 * Proporciona endpoints para crear, obtener, listar, actualizar y eliminar grupos.
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class GroupController {

    private final GroupDomainService groupDomainService;

    /**
     * Endpoint para crear un nuevo grupo.
     * Requiere que el usuario tenga rol ADMINISTRADOR o MODERADOR.
     *
     * @param request DTO con los datos para crear el grupo.
     * @return Respuesta API con el grupo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('MODERADOR')")
    public ResponseEntity<ApiResponseDTO<GroupResponseDTO>> createGroup(@Valid @RequestBody CreateGroupRequestDTO request) {
        GroupDomain group = groupDomainService.createGroup(request);
        return ResponseEntity.ok(ApiResponseDTO.<GroupResponseDTO>builder()
                .message("Grupo creado correctamente")
                .data(groupDomainService.toResponse(group))
                .build());
    }

    /**
     * Endpoint para obtener un grupo por su ID.
     *
     * @param id Identificador del grupo.
     * @return Respuesta API con el grupo encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GroupResponseDTO>> getGroupById(@PathVariable Integer id) {
        GroupDomain group = groupDomainService.getGroupById(id);
        return ResponseEntity.ok(ApiResponseDTO.<GroupResponseDTO>builder()
                .message("Grupo recuperado correctamente")
                .data(groupDomainService.toResponse(group))
                .build());
    }

    /**
     * Endpoint para listar todos los grupos.
     *
     * @return Respuesta API con la lista de grupos.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<GroupResponseDTO>>> getAllGroups() {
        List<GroupResponseDTO> groups = groupDomainService.toResponseList(groupDomainService.getAllGroups());
        return ResponseEntity.ok(ApiResponseDTO.<List<GroupResponseDTO>>builder()
                .message("Grupos recuperados correctamente")
                .data(groups)
                .build());
    }

    /**
     * Endpoint para actualizar la información de un grupo.
     * Requiere que el usuario tenga rol ADMINISTRADOR o MODERADOR.
     *
     * @param id      Identificador del grupo.
     * @param request DTO con los datos a actualizar.
     * @return Respuesta API con el grupo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('MODERADOR')")
    public ResponseEntity<ApiResponseDTO<GroupResponseDTO>> updateGroup(@PathVariable Integer id,
                                                                        @Valid @RequestBody UpdateGroupRequestDTO request) {
        GroupDomain group = groupDomainService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<GroupResponseDTO>builder()
                .message("Grupo actualizado correctamente")
                .data(groupDomainService.toResponse(group))
                .build());
    }

    /**
     * Endpoint para eliminar un grupo.
     * Solo se permite a usuarios con rol ADMINISTRADOR.
     *
     * @param id Identificador del grupo a eliminar.
     * @return Respuesta API confirmando la eliminación.    
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteGroup(@PathVariable Integer id) {
        groupDomainService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Grupo eliminado correctamente")
                .build());
    }
    /**
     * Endpoint para que el usuario autenticado se una a un grupo.
     *
     * @param groupId ID del grupo al que se desea unir.
     * @return Respuesta API confirmando la unión.
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponseDTO<Void>> joinGroup(@PathVariable Integer groupId) {
        groupDomainService.joinGroup(groupId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Usuario unido al grupo correctamente")
                .build());
    }
}
