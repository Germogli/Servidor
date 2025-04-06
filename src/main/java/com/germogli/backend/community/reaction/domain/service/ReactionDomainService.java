package com.germogli.backend.community.reaction.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.community.reaction.application.dto.CreateReactionRequestDTO;
import com.germogli.backend.community.reaction.application.dto.ReactionResponseDTO;
import com.germogli.backend.community.reaction.domain.model.ReactionDomain;
import com.germogli.backend.community.reaction.domain.repository.ReactionDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de reacciones en publicaciones.
 * Contiene la lógica para crear, obtener, listar y eliminar reacciones.
 * Gestiona notificaciones en tiempo real y validaciones de permisos.
 */
@Service
@RequiredArgsConstructor
public class ReactionDomainService {

    private final ReactionDomainRepository reactionRepository;
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea una nueva reacción sobre una publicación.
     * Valida la existencia de la publicación y notifica al dueño si corresponde.
     *
     * @param request DTO con los datos para crear la reacción.
     * @return Reacción creada.
     */
    public ReactionDomain createReaction(CreateReactionRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Validar existencia del post
        sharedService.validatePostExists(request.getPostId());

        // Construir y persistir la reacción
        ReactionDomain reaction = ReactionDomain.builder()
                .postId(request.getPostId())
                .userId(currentUser.getId())
                .reactionType(request.getReactionType())
                .build();

        ReactionDomain savedReaction = reactionRepository.save(reaction);

        // Obtener el dueño del post y notificar si no es el mismo que reaccionó
        Integer postOwnerId = sharedService.getOwnerIdOfPost(request.getPostId());

        if (!postOwnerId.equals(currentUser.getId())) {
            notificationService.sendNotification(
                    postOwnerId,
                    "Tu publicación recibió una nueva reacción",
                    "reaction"
            );
        }

        return savedReaction;
    }

    /**
     * Obtiene una reacción por su identificador único.
     *
     * @param id Identificador de la reacción.
     * @return Reacción encontrada.
     * @throws ResourceNotFoundException si no se encuentra.
     */
    public ReactionDomain getReactionById(Integer id) {
        return reactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reacción no encontrada con id: " + id));
    }

    /**
     * Lista todas las reacciones registradas en el sistema.
     *
     * @return Lista de reacciones.
     */
    public List<ReactionDomain> getAllReactions() {
        return reactionRepository.findAll();
    }

    /**
     * Elimina una reacción del sistema.
     * Solo el usuario que creó la reacción puede eliminarla.
     *
     * @param id Identificador de la reacción.
     * @throws AccessDeniedException si el usuario no es el propietario.
     */
    public void deleteReaction(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        ReactionDomain reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reacción no encontrada con id: " + id));

        // Validar que el usuario es el autor de la reacción
        if (!reaction.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta reacción");
        }

        reactionRepository.deleteById(id);
    }

    /**
     * Convierte una entidad de dominio Reaction en un DTO de respuesta.
     *
     * @param reaction Reacción a convertir.
     * @return DTO representando la reacción.
     */
    public ReactionResponseDTO toResponse(ReactionDomain reaction) {
        return ReactionResponseDTO.builder()
                .id(reaction.getId())
                .postId(reaction.getPostId())
                .userId(reaction.getUserId())
                .reactionType(reaction.getReactionType())
                .reactionDate(reaction.getReactionDate())
                .build();
    }

    /**
     * Convierte una lista de entidades de dominio Reaction en DTOs de respuesta.
     *
     * @param reactions Lista de reacciones.
     * @return Lista de DTOs.
     */
    public List<ReactionResponseDTO> toResponseList(List<ReactionDomain> reactions) {
        return reactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
