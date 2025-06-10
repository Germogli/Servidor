package com.germogli.backend.community.thread.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.thread.application.dto.CreateThreadRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadResponseDTO;
import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.repository.ThreadDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de hilos.
 * Contiene la lógica para crear, obtener, actualizar y eliminar hilos.
 */
@Service
@RequiredArgsConstructor
public class ThreadDomainService {

    private final ThreadDomainRepository threadRepository;
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;


    /**
     * Crea un nuevo hilo.
     * Se establece la fecha de creación y se puede calcular la fecha de expiración (2 días).
     *
     * @param request DTO con los datos del hilo.
     * @return El hilo creado.
     */
    public ThreadDomain createThread(CreateThreadRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(2);

        ThreadDomain thread = ThreadDomain.builder()
                .groupId(request.getGroupId())
                .userId(currentUser.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .creationDate(now)
                .build();

        ThreadDomain savedThread = threadRepository.saveThread(thread);

        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha creado un nuevo hilo: " + request.getTitle(),
                "thread"
        );

        return savedThread;
    }

    /**
     * Obtiene un hilo por su ID.
     *
     * @param id Identificador del hilo.
     * @return El hilo encontrado.
     * @throws ResourceNotFoundException si no se encuentra el hilo.
     */
    public ThreadDomain getThreadById(Integer id) {
        return threadRepository.findThreadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thread no encontrado con id: " + id));
    }

    /**
     * Obtiene todos los hilos no expirados.
     *
     * @return Lista de hilos activos.
     */
    public List<ThreadDomain> getAllThreads() {
        List<ThreadDomain> threads = threadRepository.findAllThreads();
        return threads.stream()
                .filter(t -> t.getCreationDate().plusDays(2).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un hilo existente.
     * Solo el creador del hilo puede actualizarlo.
     *
     * @param id      Identificador del hilo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return El hilo actualizado.
     */
    public ThreadDomain updateThread(Integer id, CreateThreadRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        ThreadDomain existingThread = threadRepository.findThreadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thread no encontrado con id: " + id));

        if (!existingThread.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para actualizar este thread");
        }

        existingThread.setTitle(request.getTitle());
        existingThread.setContent(request.getContent());

        ThreadDomain updatedThread = threadRepository.saveThread(existingThread);

        notificationService.sendNotification(
                currentUser.getId(),
                "Su hilo ha sido actualizado",
                "thread"
        );

        return updatedThread;
    }

    /**
     * Elimina un hilo.
     * Solo el creador o un administrador pueden eliminarlo.
     *
     * @param id Identificador del hilo a eliminar.
     */
    public void deleteThread(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        ThreadDomain thread = threadRepository.findThreadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thread no encontrado con id: " + id));

        boolean isOwner = thread.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar este thread");
        }

        threadRepository.deleteThreadById(id);

        notificationService.sendNotification(
                currentUser.getId(),
                "Su hilo ha sido eliminado",
                "thread"
        );
    }

    /**
     * Convierte un objeto ThreadDomain a su representación en DTO.
     */
    public ThreadResponseDTO toThreadResponse(ThreadDomain thread) {
        return ThreadResponseDTO.builder()
                .id(thread.getId())
                .groupId(thread.getGroupId())
                .userId(thread.getUserId())
                .title(thread.getTitle())
                .content(thread.getContent())
                .creationDate(thread.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de hilos a DTOs de respuesta.
     */
    public List<ThreadResponseDTO> toThreadResponseList(List<ThreadDomain> threads) {
        return threads.stream().map(this::toThreadResponse).collect(Collectors.toList());
    }

    /**
     * Método utilizado por tareas programadas para eliminar hilos automáticamente.
     *
     * @param id Identificador del hilo a eliminar.
     */
    @Transactional
    public void deleteThreadAsSystem(Integer id) {
        ThreadDomain thread = threadRepository.findThreadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thread no encontrado con id: " + id));

        threadRepository.deleteThreadById(id);

        notificationService.sendNotification(
                null,
                "Hilo eliminado por el sistema",
                "thread"
        );
    }
    /**
     * Obtiene todos los hilos que pertenecen a un grupo específico.
     *
     * @param groupId ID del grupo
     * @return Lista de hilos del grupo
     * @throws ResourceNotFoundException si no se encuentran hilos en el grupo
     */
    public List<ThreadDomain> getThreadsByGroupId(Integer groupId) {
        // Verificar que el grupo exista
        sharedService.validateGroupExists(groupId);

        List<ThreadDomain> threads = threadRepository.findThreadsByGroupId(groupId);
        return threads.stream()
                .filter(t -> t.getCreationDate().plusDays(2).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }
    /**
     * Obtiene todos los hilos que pertenecen al foro general (sin grupo asociado).
     *
     * @return Lista de hilos del foro
     */
    public List<ThreadDomain> getForumThreads() {
        List<ThreadDomain> threads = threadRepository.findForumThreads();
        return threads.stream()
                .filter(t -> t.getCreationDate().plusDays(2).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los hilos creados por un usuario específico.
     * Incluye tanto hilos del foro como hilos de grupos.
     *
     * @param userId ID del usuario o null para usar el usuario autenticado actual
     * @return Lista de hilos del usuario
     */
    public List<ThreadDomain> getThreadsByUserId(Integer userId) {
        // Si no se proporciona ID, usar el usuario autenticado
        Integer targetUserId = userId;
        if (targetUserId == null) {
            UserDomain currentUser = sharedService.getAuthenticatedUser();
            targetUserId = currentUser.getId();
        }

        final Integer finalUserId = targetUserId;

        List<ThreadDomain> threads = threadRepository.findThreadsByUserId(finalUserId);
        return threads.stream()
                .filter(t -> t.getCreationDate().plusDays(2).isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }
}
