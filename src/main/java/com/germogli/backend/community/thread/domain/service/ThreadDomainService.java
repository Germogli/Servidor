package com.germogli.backend.community.thread.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.notification.NotificationPublisher;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.model.ThreadReplyDomain;
import com.germogli.backend.community.thread.domain.repository.ThreadDomainRepository;
import com.germogli.backend.community.thread.application.dto.CreateThreadRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadResponseDTO;
import com.germogli.backend.community.thread.application.dto.CreateThreadReplyRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadReplyResponseDTO;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de hilos y respuestas.
 * Contiene la lógica para crear, obtener, actualizar y eliminar hilos (threads) y respuestas (thread replies).
 */
@Service
@RequiredArgsConstructor
public class ThreadDomainService {

    // Repositorio para operaciones de persistencia de hilos y respuestas.
    private final ThreadDomainRepository threadRepository;
    // Servicio compartido para obtener el usuario autenticado y verificar roles.
    private final CommunitySharedService sharedService;
    // Servicio para enviar notificaciones.
    private final NotificationPublisher notificationPublisher;

    /**
     * Crea un nuevo hilo.
     * Asigna el usuario autenticado como creador y utiliza el procedimiento almacenado para crear el hilo.
     *
     * @param request DTO con los datos del hilo.
     * @return El hilo creado.
     */
    public ThreadDomain createThread(CreateThreadRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        ThreadDomain thread = ThreadDomain.builder()
                .groupId(request.getGroupId())
                .userId(currentUser.getId())
                .title(request.getTitle())
                .content(request.getContent())
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
     *
     * @return Lista de hilos.
     */
    public List<ThreadDomain> getAllThreads() {
        return threadRepository.findAllThreads();
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
     * Convierte un ThreadDomain en un DTO de respuesta.
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

    // --- Operaciones para respuestas (thread replies) ---

    /**
     * Crea una respuesta para un hilo.
     *
     * @param request DTO con los datos de la respuesta.
     * @return La respuesta creada.
     */
    public ThreadReplyDomain createThreadReply(CreateThreadReplyRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        ThreadReplyDomain reply = ThreadReplyDomain.builder()
                .threadId(request.getThreadId())
                .userId(currentUser.getId())
                .content(request.getContent())
                .build();
        ThreadReplyDomain savedReply = threadRepository.saveThreadReply(reply);
        notificationPublisher.publishNotification(currentUser.getId(),
                "Se ha agregado una respuesta en el hilo",
                "threadReply");
        return savedReply;
    }

    /**
     * Obtiene todas las respuestas de un hilo.
     *
     * @param threadId Identificador del hilo.
     * @return Lista de respuestas.
     */
    public List<ThreadReplyDomain> getRepliesByThreadId(Integer threadId) {
        return threadRepository.findAllRepliesByThreadId(threadId);
    }

    /**
     * Elimina una respuesta de un hilo.
     * Solo el creador o un administrador pueden eliminar la respuesta.
     *
     * @param id Identificador de la respuesta a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    public void deleteThreadReply(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        ThreadReplyDomain reply = threadRepository.findThreadReplyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reply no encontrado con id: " + id));
        boolean isOwner = reply.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta respuesta");
        }
        threadRepository.deleteThreadReplyById(id);
        notificationPublisher.publishNotification(currentUser.getId(),
                "Su respuesta ha sido eliminada",
                "threadReply");
    }

    /**
     * Convierte un ThreadReplyDomain en un DTO de respuesta.
     *
     * @param reply Respuesta a convertir.
     * @return DTO con los datos de la respuesta.
     */
    public ThreadReplyResponseDTO toThreadReplyResponse(ThreadReplyDomain reply) {
        return ThreadReplyResponseDTO.builder()
                .id(reply.getId())
                .threadId(reply.getThreadId())
                .userId(reply.getUserId())
                .content(reply.getContent())
                .creationDate(reply.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de ThreadReplyDomain en una lista de DTOs de respuesta.
     *
     * @param replies Lista de respuestas.
     * @return Lista de DTOs.
     */
    public List<ThreadReplyResponseDTO> toThreadReplyResponseList(List<ThreadReplyDomain> replies) {
        return replies.stream().map(this::toThreadReplyResponse).collect(Collectors.toList());
    }
}
