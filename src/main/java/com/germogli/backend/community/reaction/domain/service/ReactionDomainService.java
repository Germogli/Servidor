package com.germogli.backend.community.reaction.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.notification.NotificationPublisher;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.reaction.domain.model.ReactionDomain;
import com.germogli.backend.community.reaction.domain.repository.ReactionDomainRepository;
import com.germogli.backend.community.reaction.application.dto.CreateReactionRequestDTO;
import com.germogli.backend.community.reaction.application.dto.ReactionResponseDTO;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de reacciones.
 * Contiene la lógica para crear, obtener, listar y eliminar reacciones.
 */
@Service
@RequiredArgsConstructor
public class ReactionDomainService {

    private final ReactionDomainRepository reactionRepository;
    private final CommunitySharedService sharedService;
    private final NotificationPublisher notificationPublisher;

    /**
     * Crea una nueva reacción.
     *
     * @param request DTO con los datos para crear la reacción.
     * @return Reacción creada.
     */
    public ReactionDomain createReaction(CreateReactionRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        ReactionDomain reaction = ReactionDomain.builder()
                .postId(request.getPostId())
                .userId(currentUser.getId())
                .reactionType(request.getReactionType())
                .build();
        ReactionDomain savedReaction = reactionRepository.save(reaction);
        notificationPublisher.publishNotification(currentUser.getId(),
                "Se ha agregado una reacción",
                "reaction");
        return savedReaction;
    }

    /**
     * Obtiene una reacción por su ID.
     *
     * @param id Identificador de la reacción.
     * @return Reacción encontrada.
     * @throws ResourceNotFoundException si no se encuentra la reacción.
     */
    public ReactionDomain getReactionById(Integer id) {
        return reactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reacción no encontrada con id: " + id));
    }

    /**
     * Obtiene todas las reacciones.
     *
     * @return Lista de reacciones.
     */
    public List<ReactionDomain> getAllReactions() {
        return reactionRepository.findAll();
    }

    /**
     * Elimina una reacción.
     * Solo el propietario o un administrador pueden eliminarla.
     *
     * @param id Identificador de la reacción a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    public void deleteReaction(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        ReactionDomain reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reacción no encontrada con id: " + id));
        boolean isOwner = reaction.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta reacción");
        }
        reactionRepository.deleteById(id);
        notificationPublisher.publishNotification(currentUser.getId(),
                "La reacción ha sido eliminada",
                "reaction");
    }

    /**
     * Convierte un objeto ReactionDomain en un DTO de respuesta.
     *
     * @param reaction Reacción a convertir.
     * @return DTO con la información de la reacción.
     */
    public ReactionResponseDTO toResponse(ReactionDomain reaction) {
        return ReactionResponseDTO.builder()
                .id(reaction.getId())
                .postId(reaction.getPostId())
                .userId(reaction.getUserId())
                .reactionType(reaction.getReactionType())
                .creationDate(reaction.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de ReactionDomain en una lista de DTOs de respuesta.
     *
     * @param reactions Lista de reacciones.
     * @return Lista de DTOs.
     */
    public List<ReactionResponseDTO> toResponseList(List<ReactionDomain> reactions) {
        return reactions.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
