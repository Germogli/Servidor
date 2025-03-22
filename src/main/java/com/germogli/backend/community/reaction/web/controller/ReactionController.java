package com.germogli.backend.community.reaction.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.reaction.application.dto.CreateReactionRequestDTO;
import com.germogli.backend.community.reaction.application.dto.ReactionResponseDTO;
import com.germogli.backend.community.reaction.domain.model.ReactionDomain;
import com.germogli.backend.community.reaction.domain.service.ReactionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de reacciones en Community.
 * Proporciona endpoints para crear, obtener, listar y eliminar reacciones.
 */
@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionDomainService reactionDomainService;

    /**
     * Endpoint para crear una nueva reacción.
     *
     * @param request DTO con los datos para crear la reacción.
     * @return Respuesta API con la reacción creada.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReactionResponseDTO>> createReaction(@Valid @RequestBody CreateReactionRequestDTO request) {
        ReactionDomain reaction = reactionDomainService.createReaction(request);
        return ResponseEntity.ok(ApiResponseDTO.<ReactionResponseDTO>builder()
                .message("Reacción creada correctamente")
                .data(reactionDomainService.toResponse(reaction))
                .build());
    }

    /**
     * Endpoint para obtener una reacción por su ID.
     *
     * @param id Identificador de la reacción.
     * @return Respuesta API con la reacción encontrada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ReactionResponseDTO>> getReactionById(@PathVariable Integer id) {
        ReactionDomain reaction = reactionDomainService.getReactionById(id);
        return ResponseEntity.ok(ApiResponseDTO.<ReactionResponseDTO>builder()
                .message("Reacción recuperada correctamente")
                .data(reactionDomainService.toResponse(reaction))
                .build());
    }

    /**
     * Endpoint para listar todas las reacciones.
     *
     * @return Respuesta API con la lista de reacciones.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ReactionResponseDTO>>> getAllReactions() {
        List<ReactionResponseDTO> reactions = reactionDomainService.toResponseList(reactionDomainService.getAllReactions());
        return ResponseEntity.ok(ApiResponseDTO.<List<ReactionResponseDTO>>builder()
                .message("Reacciones recuperadas correctamente")
                .data(reactions)
                .build());
    }

    /**
     * Endpoint para eliminar una reacción.
     *
     * @param id Identificador de la reacción a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteReaction(@PathVariable Integer id) {
        reactionDomainService.deleteReaction(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Reacción eliminada correctamente")
                .build());
    }
}
