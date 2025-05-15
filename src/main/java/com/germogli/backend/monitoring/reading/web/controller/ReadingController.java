package com.germogli.backend.monitoring.reading.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingBatchRequestDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingRequestDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingResponseDTO;
import com.germogli.backend.monitoring.reading.domain.model.ReadingDomain;
import com.germogli.backend.monitoring.reading.domain.service.ReadingDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de lecturas de sensores.
 * Proporciona endpoints para crear y consultar lecturas de sensores.
 */
@RestController
@RequestMapping("/readings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReadingController {

    private final ReadingDomainService readingDomainService;

    /**
     * Endpoint para crear una nueva lectura de sensor.
     *
     * @param request DTO con los datos de la lectura.
     * @return Respuesta API con la lectura creada.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReadingResponseDTO>> createReading(@Valid @RequestBody ReadingRequestDTO request) {
        ReadingDomain reading = readingDomainService.createReading(request);
        return ResponseEntity.ok(ApiResponseDTO.<ReadingResponseDTO>builder()
                .message("Lectura registrada correctamente")
                .data(readingDomainService.toResponse(reading))
                .build());
    }

    /**
     * Endpoint para procesar un lote de lecturas desde dispositivos IoT.
     * No requiere autenticación ya que los dispositivos IoT utilizan su propio mecanismo.
     *
     * @param request DTO con los datos del lote.
     * @return Respuesta API con la lista de lecturas creadas.
     */
    @PostMapping("/batch")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponseDTO<List<ReadingResponseDTO>>> processBatchReadings(
            @Valid @RequestBody ReadingBatchRequestDTO request) {
        List<ReadingDomain> readings = readingDomainService.processBatchReadings(request);
        return ResponseEntity.ok(ApiResponseDTO.<List<ReadingResponseDTO>>builder()
                .message("Lecturas procesadas correctamente")
                .data(readingDomainService.toResponseList(readings))
                .build());
    }

    /**
     * Endpoint para obtener una lectura por su ID.
     *
     * @param id Identificador de la lectura.
     * @return Respuesta API con la lectura encontrada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ReadingResponseDTO>> getReadingById(@PathVariable Integer id) {
        ReadingDomain reading = readingDomainService.getReadingById(id);
        return ResponseEntity.ok(ApiResponseDTO.<ReadingResponseDTO>builder()
                .message("Lectura recuperada correctamente")
                .data(readingDomainService.toResponse(reading))
                .build());
    }

    /**
     * Endpoint para obtener todas las lecturas de un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Respuesta API con la lista de lecturas del cultivo.
     */
    @GetMapping("/crop/{cropId}")
    public ResponseEntity<ApiResponseDTO<List<ReadingResponseDTO>>> getReadingsByCropId(@PathVariable Integer cropId) {
        List<ReadingResponseDTO> readings = readingDomainService.toResponseList(
                readingDomainService.getReadingsByCropId(cropId));
        return ResponseEntity.ok(ApiResponseDTO.<List<ReadingResponseDTO>>builder()
                .message("Lecturas del cultivo recuperadas correctamente")
                .data(readings)
                .build());
    }

    /**
     * Endpoint para obtener el historial de lecturas de un sensor específico en un cultivo,
     * dentro de un rango de fechas.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param startDate Fecha de inicio del rango (opcional).
     * @param endDate Fecha de fin del rango (opcional).
     * @param limit Límite de registros a recuperar (opcional).
     * @return Respuesta API con la lista de lecturas que cumplen con los criterios.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDTO<List<ReadingResponseDTO>>> getReadingHistory(
            @RequestParam Integer cropId,
            @RequestParam Integer sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer limit) {

        List<ReadingResponseDTO> readings = readingDomainService.toResponseList(
                readingDomainService.getReadingHistory(cropId, sensorId, startDate, endDate, limit));

        return ResponseEntity.ok(ApiResponseDTO.<List<ReadingResponseDTO>>builder()
                .message("Historial de lecturas recuperado correctamente")
                .data(readings)
                .build());
    }

}