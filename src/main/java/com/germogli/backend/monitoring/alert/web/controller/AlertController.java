package com.germogli.backend.monitoring.alert.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.alert.application.dto.AlertResponseDTO;
import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;
import com.germogli.backend.monitoring.alert.domain.service.AlertDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de alertas.
 * Proporciona endpoints para crear y consultar alertas.
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AlertController {

    private final AlertDomainService alertDomainService;

    /**
     * Endpoint para obtener una alerta por su ID.
     *
     * @param id Identificador de la alerta.
     * @return Respuesta API con la alerta encontrada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AlertResponseDTO>> getAlertById(@PathVariable Integer id) {
        AlertDomain alert = alertDomainService.getAlertById(id);
        return ResponseEntity.ok(ApiResponseDTO.<AlertResponseDTO>builder()
                .message("Alerta recuperada correctamente")
                .data(alertDomainService.toResponse(alert))
                .build());
    }

    /**
     * Endpoint para obtener todas las alertas de un cultivo específico.
     *
     * @param cropId Identificador del cultivo.
     * @return Respuesta API con la lista de alertas del cultivo.
     */
    @GetMapping("/crop/{cropId}")
    public ResponseEntity<ApiResponseDTO<List<AlertResponseDTO>>> getAlertsByCropId(@PathVariable Integer cropId) {
        List<AlertResponseDTO> alerts = alertDomainService.toResponseList(
                alertDomainService.getAlertsByCropId(cropId));
        return ResponseEntity.ok(ApiResponseDTO.<List<AlertResponseDTO>>builder()
                .message("Alertas del cultivo recuperadas correctamente")
                .data(alerts)
                .build());
    }

    /**
     * Endpoint para obtener todas las alertas de un nivel específico para los cultivos del usuario.
     * Los administradores pueden ver alertas de todos los cultivos.
     *
     * @param level Nivel de alerta (low, medium, high, critical).
     * @return Respuesta API con la lista de alertas del nivel especificado.
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponseDTO<List<AlertResponseDTO>>> getAlertsByLevel(@PathVariable String level) {
        List<AlertResponseDTO> alerts = alertDomainService.toResponseList(
                alertDomainService.getAlertsByLevel(level));
        return ResponseEntity.ok(ApiResponseDTO.<List<AlertResponseDTO>>builder()
                .message("Alertas del nivel " + level + " recuperadas correctamente")
                .data(alerts)
                .build());
    }

    /**
     * Endpoint para obtener todas las alertas de un cultivo específico en un rango de fechas.
     *
     * @param cropId Identificador del cultivo.
     * @param startDate Fecha de inicio del rango (opcional).
     * @param endDate Fecha de fin del rango (opcional).
     * @return Respuesta API con la lista de alertas que cumplen con los criterios.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDTO<List<AlertResponseDTO>>> getAlertsByCropIdAndDateRange(
            @RequestParam Integer cropId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AlertResponseDTO> alerts = alertDomainService.toResponseList(
                alertDomainService.getAlertsByCropIdAndDateRange(cropId, startDate, endDate));

        return ResponseEntity.ok(ApiResponseDTO.<List<AlertResponseDTO>>builder()
                .message("Historial de alertas recuperado correctamente")
                .data(alerts)
                .build());
    }

    /**
     * Endpoint para crear manualmente una alerta para un cultivo y sensor específicos.
     * Solo los administradores pueden crear alertas manualmente.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param alertLevel Nivel de alerta.
     * @param alertMessage Mensaje de alerta.
     * @return Respuesta API con la alerta creada.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<AlertResponseDTO>> createAlert(
            @RequestParam Integer cropId,
            @RequestParam Integer sensorId,
            @RequestParam String alertLevel,
            @RequestParam String alertMessage) {

        AlertDomain alert = alertDomainService.createAlert(cropId, sensorId, alertLevel, alertMessage);

        return ResponseEntity.ok(ApiResponseDTO.<AlertResponseDTO>builder()
                .message("Alerta creada correctamente")
                .data(alertDomainService.toResponse(alert))
                .build());
    }

    /**
     * Endpoint para eliminar una alerta.
     * Solo los administradores pueden eliminar alertas.
     *
     * @param id Identificador de la alerta a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAlert(@PathVariable Integer id) {
        alertDomainService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Alerta eliminada correctamente")
                .build());
    }
}