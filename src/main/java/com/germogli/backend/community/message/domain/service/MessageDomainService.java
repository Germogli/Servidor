package com.germogli.backend.community.message.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.repository.MessageDomainRepository;
import com.germogli.backend.community.message.application.dto.CreateMessageRequestDTO;
import com.germogli.backend.community.message.application.dto.MessageResponseDTO;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de mensajes.
 * Contiene la lógica de negocio para crear, obtener, listar y eliminar mensajes.
 */
@Service
@RequiredArgsConstructor
public class MessageDomainService {

    private final MessageDomainRepository messageRepository;
    private final CommunitySharedService sharedService;
    /**
     * Crea un mensaje utilizando los datos del DTO.
     *
     * @param request DTO con la información para crear el mensaje.
     * @return El mensaje creado.
     */
    public MessageDomain createMessage(CreateMessageRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        MessageDomain message = MessageDomain.builder()
                .postId(request.getPostId())
                .userId(currentUser.getId())
                .content(request.getContent())
                .threadId(request.getThreadId())
                .groupId(request.getGroupId())
                .build();
        MessageDomain savedMessage = messageRepository.save(message);
        return savedMessage;
    }

    /**
     * Obtiene un mensaje por su ID.
     *
     * @param id Identificador del mensaje.
     * @return El mensaje encontrado.
     * @throws ResourceNotFoundException si el mensaje no existe.
     */
    public MessageDomain getMessageById(Integer id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado con id: " + id));
    }

    /**
     * Obtiene todos los mensajes.
     *
     * @return Lista de mensajes.
     * @throws ResourceNotFoundException si no hay mensajes disponibles.
     */
    public List<MessageDomain> getAllMessages() {
        List<MessageDomain> messages = messageRepository.findAll();
        if (messages.isEmpty()) {
            throw new ResourceNotFoundException("No hay mensajes disponibles.");
        }
        return messages;
    }

    /**
     * Elimina un mensaje.
     * Verifica que el usuario autenticado sea el propietario o tenga rol de ADMINISTRADOR.
     *
     * @param id Identificador del mensaje a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    public void deleteMessage(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        MessageDomain message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado con id: " + id));
        boolean isOwner = message.getUserId().equals(currentUser.getId());
        boolean isPrivileged = sharedService.hasRole(currentUser, "ADMINISTRADOR")
                || sharedService.hasRole(currentUser, "MODERADOR");
        if (!isOwner && !isPrivileged) {
            throw new AccessDeniedException("No tiene permisos para eliminar este mensaje");
        }
        messageRepository.deleteById(id);

    }


    /**
     * Convierte un objeto MessageDomain en un MessageResponseDTO para enviar en la API.
     *
     * @param message Mensaje a convertir.
     * @return DTO con la información del mensaje.
     */
    public MessageResponseDTO toResponse(MessageDomain message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .postId(message.getPostId())
                .userId(message.getUserId())
                .content(message.getContent())
                .threadId(message.getThreadId())
                .groupId(message.getGroupId())
                .creationDate(message.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de MessageDomain en una lista de MessageResponseDTO.
     *
     * @param messages Lista de mensajes.
     * @return Lista de DTOs con la información de los mensajes.
     */
    public List<MessageResponseDTO> toResponseList(List<MessageDomain> messages) {
        return messages.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
