package com.germogli.backend.monitoring.alert.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.alert.application.dto.AlertResponseDTO;
import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;
import com.germogli.backend.monitoring.alert.domain.service.AlertDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de alertas.
 * Proporciona endpoints para consultar y eliminar alertas.
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
     * Endpoint para obtener todas las alertas del usuario autenticado.
     *
     * @return Respuesta API con la lista de alertas del usuario.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponseDTO<List<AlertResponseDTO>>> getUserAlerts() {
        List<AlertResponseDTO> alerts = alertDomainService.toResponseList(
                alertDomainService.getUserAlerts());
        return ResponseEntity.ok(ApiResponseDTO.<List<AlertResponseDTO>>builder()
                .message("Alertas del usuario recuperadas correctamente")
                .data(alerts)
                .build());
    }

    /**
     * Endpoint para eliminar una alerta.
     * Solo el propietario del cultivo puede eliminar la alerta.
     *
     * @param id Identificador de la alerta a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAlert(@PathVariable Integer id) {
        alertDomainService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Alerta eliminada correctamente")
                .build());
    }
}