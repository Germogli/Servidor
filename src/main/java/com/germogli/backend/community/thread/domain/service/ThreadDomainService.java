package com.germogli.backend.community.thread.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.notification.NotificationPublisher;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.repository.ThreadDomainRepository;
import com.germogli.backend.community.thread.application.dto.CreateThreadRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadResponseDTO;
import com.germogli.backend.common.exception.ResourceNotFoundException;
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
    private final NotificationPublisher notificationPublisher;

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
        // Opcional: calcular fecha de expiración (2 días después)
        LocalDateTime expirationDate = now.plusDays(2);
        // Se puede almacenar en el dominio de forma transitoria o gestionar su eliminación mediante un scheduled task.
        ThreadDomain thread = ThreadDomain.builder()
                .groupId(request.getGroupId())
                .userId(currentUser.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .creationDate(now)
                .build();
        ThreadDomain savedThread = threadRepository.saveThread(thread);
        notificationPublisher.publishNotification(currentUser.getId(),
                "Se ha creado un nuevo hilo: " + request.getTitle(),
                "thread");
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
     * Obtiene todos los hilos.
     * Opcionalmente se podrían filtrar los hilos expirados.
     *
     * @return Lista de hilos.
     */
    public List<ThreadDomain> getAllThreads() {
        List<ThreadDomain> threads = threadRepository.findAllThreads();
        // Opcional: filtrar hilos expirados (por ejemplo, aquellos con creationDate + 2 días < now)
        // threads = threads.stream().filter(t -> t.getCreationDate().plusDays(2).isAfter(LocalDateTime.now())).collect(Collectors.toList());
        return threads;
    }

    /**
     * Actualiza un hilo.
     * Solo el creador del hilo puede actualizarlo.
     *
     * @param id      Identificador del hilo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return El hilo actualizado.
     * @throws AccessDeniedException si el usuario no es el propietario.
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
        notificationPublisher.publishNotification(currentUser.getId(),
                "Su hilo ha sido actualizado",
                "thread");
        return updatedThread;
    }

    /**
     * Elimina un hilo.
     * Solo el creador o un administrador pueden eliminar el hilo.
     *
     * @param id Identificador del hilo a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
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
        notificationPublisher.publishNotification(currentUser.getId(),
                "Su hilo ha sido eliminado",
                "thread");
    }

    /**
     * Convierte un objeto ThreadDomain en un DTO de respuesta.
     *
     * @param thread Hilo a convertir.
     * @return DTO con los datos del hilo.
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
     * Convierte una lista de ThreadDomain en una lista de DTOs de respuesta.
     *
     * @param threads Lista de hilos.
     * @return Lista de DTOs.
     */
    public List<ThreadResponseDTO> toThreadResponseList(List<ThreadDomain> threads) {
        return threads.stream().map(this::toThreadResponse).collect(Collectors.toList());
    }
    /**
     * Metodo que utiliza la tarea programada para eliminar un hilo.
     *
     * @param id Identificador del hilo a eliminar.
     * @return void.
     */
    @Transactional
    public void deleteThreadAsSystem(Integer id) {
        // Se omite la validación del usuario (SecurityContext)
        ThreadDomain thread = threadRepository.findThreadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thread no encontrado con id: " + id));
        threadRepository.deleteThreadById(id);
        // Nota: Si la notificación no es necesaria en este caso, se puede omitir
        notificationPublisher.publishNotification(null,
                "Hilo eliminado por el sistema",
                "thread");
    }
}
