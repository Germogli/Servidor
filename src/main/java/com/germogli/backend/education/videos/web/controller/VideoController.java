package com.germogli.backend.education.videos.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.videos.application.dto.CreateVideoRequestDTO;
import com.germogli.backend.education.videos.application.dto.UpdateVideoRequestDTO;
import com.germogli.backend.education.videos.application.dto.VideoResponseDTO;
import com.germogli.backend.education.videos.domain.model.VideoDomain;
import com.germogli.backend.education.videos.domain.service.VideoDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar videos en el módulo Education.
 */
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VideoController {

    private final VideoDomainService videoDomainService;

    /**
     * Crea un nuevo video educativo.
     *
     * @param dto Objeto CreateVideoRequestDTO con la información del video.
     * @return ResponseEntity con el video creado en un ApiResponseDTO.
     */
    @PostMapping()
    public ResponseEntity<ApiResponseDTO<VideoResponseDTO>> createVideo(@RequestBody CreateVideoRequestDTO dto) {
        VideoDomain createdVideo = videoDomainService.createVideo(dto);
        return ResponseEntity.ok(
                ApiResponseDTO.<VideoResponseDTO>builder()
                        .message("Video creado correctamente")
                        .data(VideoResponseDTO.fromDomain(createdVideo))
                        .build()
        );
    }

    /**
     * Obtiene un video por su ID.
     *
     * @param id ID del video a buscar.
     * @return ResponseEntity con el video en un ApiResponseDTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VideoResponseDTO>> getVideoById(@PathVariable Integer id) {
        VideoDomain video = videoDomainService.getVideoById(id);
        return ResponseEntity.ok(
                ApiResponseDTO.<VideoResponseDTO>builder()
                        .message("Video recuperado correctamente")
                        .data(VideoResponseDTO.fromDomain(video))
                        .build()
        );
    }

    /**
     * Obtiene todos los videos asociados a un módulo específico.
     *
     * @param moduleId ID del módulo.
     * @return ResponseEntity con la lista de videos en un ApiResponseDTO.
     */
    @GetMapping("/getByModuleId/{moduleId}")
    public ResponseEntity<ApiResponseDTO<List<VideoResponseDTO>>> getVideosByModuleId(@PathVariable Integer moduleId) {
        List<VideoResponseDTO> videos = videoDomainService.toResponseList(videoDomainService.getVideosByModuleId(moduleId));
        return ResponseEntity.ok(
                ApiResponseDTO.<List<VideoResponseDTO>>builder()
                        .message("Videos recuperados correctamente para el módulo con id " + moduleId)
                        .data(videos)
                        .build()
        );
    }

    /**
     * Actualiza los datos de un video educativo.
     *
     * @param id  ID del video a actualizar.
     * @param dto Objeto UpdateVideoRequestDTO con la nueva información.
     * @return ResponseEntity con el video actualizado en un ApiResponseDTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VideoResponseDTO>> updateVideo(
            @PathVariable Integer id,
            @RequestBody UpdateVideoRequestDTO dto
    ) {
        // Aseguramos el id en el DTO (si es necesario)
        dto.setVideoId(id);
        VideoDomain updatedVideo = videoDomainService.updateVideo(id, dto);
        return ResponseEntity.ok(
                ApiResponseDTO.<VideoResponseDTO>builder()
                        .message("Video actualizado correctamente")
                        .data(VideoResponseDTO.fromDomain(updatedVideo))
                        .build()
        );
    }

    /**
     * Elimina un video educativo según su ID.
     *
     * @param id ID del video a eliminar.
     * @return ResponseEntity con un mensaje confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVideo(@PathVariable Integer id) {
        videoDomainService.deleteVideo(id);
        return ResponseEntity.ok(
                ApiResponseDTO.<Void>builder()
                        .message("Video eliminado correctamente")
                        .build()
        );
    }
}
