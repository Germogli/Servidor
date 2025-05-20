package com.germogli.backend.community.thread.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.thread.application.dto.CreateThreadRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadResponseDTO;
import com.germogli.backend.community.thread.application.dto.UpdateThreadRequestDTO;
import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.service.ThreadDomainService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de hilos en la comunidad.
 * Proporciona endpoints para crear, obtener, listar, actualizar y eliminar hilos.
 */
@RestController
@RequestMapping("/threads")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ThreadController {

    private final ThreadDomainService threadDomainService;

    /**
     * Endpoint para crear un nuevo hilo.
     * Solo pueden crear hilos los usuarios con rol ADMINISTRADOR o MODERADOR.
     *
     * @param request DTO con los datos del hilo.
     * @return Respuesta API con el hilo creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('MODERADOR')")
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> createThread(@Valid @RequestBody CreateThreadRequestDTO request) {
        ThreadDomain thread = threadDomainService.createThread(request);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Hilo creado correctamente")
                .data(threadDomainService.toThreadResponse(thread))
                .build());
    }

    /**
     * Endpoint para obtener un hilo por su ID.
     *
     * @param id Identificador del hilo.
     * @return Respuesta API con el hilo encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> getThreadById(@PathVariable Integer id) {
        ThreadDomain thread = threadDomainService.getThreadById(id);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Hilo recuperado correctamente")
                .data(threadDomainService.toThreadResponse(thread))
                .build());
    }

    /**
     * Endpoint para listar todos los hilos.
     *
     * @return Respuesta API con la lista de hilos.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ThreadResponseDTO>>> getAllThreads() {
        List<ThreadResponseDTO> threads = threadDomainService.toThreadResponseList(threadDomainService.getAllThreads());
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadResponseDTO>>builder()
                .message("Hilos recuperados correctamente")
                .data(threads)
                .build());
    }
    // Endpoints existentes

    /**
     * Endpoint para obtener hilos de un grupo específico.
     *
     * @param groupId ID del grupo
     * @return Respuesta API con la lista de hilos del grupo
     */
    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<ApiResponseDTO<List<ThreadResponseDTO>>> getThreadsByGroupId(@PathVariable Integer groupId) {
        List<ThreadResponseDTO> threads = threadDomainService.toThreadResponseList(
                threadDomainService.getThreadsByGroupId(groupId));
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadResponseDTO>>builder()
                .message("Hilos del grupo recuperados correctamente")
                .data(threads)
                .build());
    }
    /**
     * Endpoint para obtener hilos creados por un usuario específico.
     * Si no se proporciona userId, se usará el usuario autenticado.
     *
     * @param userId ID del usuario (opcional)
     * @return Respuesta API con la lista de hilos del usuario
     */
    @GetMapping("/by-user")
    public ResponseEntity<ApiResponseDTO<List<ThreadResponseDTO>>> getThreadsByUserId(
            @RequestParam(required = false) Integer userId) {
        List<ThreadResponseDTO> threads = threadDomainService.toThreadResponseList(
                threadDomainService.getThreadsByUserId(userId));
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadResponseDTO>>builder()
                .message("Hilos del usuario recuperados correctamente")
                .data(threads)
                .build());
    }
    /**
     * Endpoint para obtener hilos del foro general (sin grupo asociado).
     *
     * @return Respuesta API con la lista de hilos del foro
     */
    @GetMapping("/forum")
    public ResponseEntity<ApiResponseDTO<List<ThreadResponseDTO>>> getForumThreads() {
        List<ThreadResponseDTO> threads = threadDomainService.toThreadResponseList(
                threadDomainService.getForumThreads());
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadResponseDTO>>builder()
                .message("Hilos del foro recuperados correctamente")
                .data(threads)
                .build());
    }

    /**
     * Endpoint para actualizar un hilo.
     * Solo el creador del hilo puede actualizarlo.
     *
     * @param id      Identificador del hilo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return Respuesta API con el hilo actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@threadSecurity.canUpdate(#id, principal)")
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> updateThread(@PathVariable Integer id,
                                                                          @Valid @RequestBody UpdateThreadRequestDTO request) {
        ThreadDomain updatedThread = threadDomainService.updateThread(id,
                new CreateThreadRequestDTO(null, request.getTitle(), request.getContent()));
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Hilo actualizado correctamente")
                .data(threadDomainService.toThreadResponse(updatedThread))
                .build());
    }

    /**
     * Endpoint para eliminar un hilo.
     * Solo el creador o un administrador pueden eliminar el hilo.
     *
     * @param id Identificador del hilo a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@threadSecurity.canDelete(#id, principal)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteThread(@PathVariable Integer id) {
        threadDomainService.deleteThread(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Hilo eliminado correctamente")
                .build());
    }
}
