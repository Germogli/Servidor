package com.germogli.backend.education.videos.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.videos.application.dto.CreateVideoRequestDTO;
import com.germogli.backend.education.videos.application.dto.VideoResponseDTO;
import com.germogli.backend.education.videos.domain.model.VideoDomain;
import com.germogli.backend.education.videos.domain.service.VideoDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar videos en el módulo Education.
 */
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
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
}
