package com.germogli.backend.monitoring.reading.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.reading.application.dto.DeviceReadingRequestDTO;
import com.germogli.backend.monitoring.reading.domain.service.ReadingDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST para la recepción de datos de dispositivos IoT.
 * Este controlador maneja las solicitudes recibidas desde dispositivos ESP32 y similares,
 * y no requiere autenticación JWT para facilitar la integración de dispositivos.
 */
@RestController
@RequestMapping("/readings/device")
@RequiredArgsConstructor
public class DeviceReadingController {

    private final ReadingDomainService readingDomainService;

    /**
     * Endpoint para recibir datos directamente de un dispositivo ESP32.
     * Recibe temperatura, humedad y TDS, y los asocia al cultivo y sensores correspondientes.
     *
     * @param deviceId ID del dispositivo (para autenticación básica y asociación)
     * @param cropId ID del cultivo al que pertenecen las lecturas
     * @param requestDTO DTO con los datos de lectura
     * @return Respuesta API confirmando la recepción
     */
    @PostMapping("/{deviceId}/crop/{cropId}")
    public ResponseEntity<ApiResponseDTO<String>> receiveDeviceReadings(
            @PathVariable Integer deviceId,
            @PathVariable Integer cropId,
            @RequestBody DeviceReadingRequestDTO requestDTO) {

        // Procesar y guardar las lecturas
        try {
            // Verificar que el dispositivo tenga los permisos necesarios
            // Por ahora, solo validamos que deviceId y cropId no sean nulos
            if (deviceId == null || cropId == null) {
                return ResponseEntity.badRequest().body(
                        ApiResponseDTO.<String>builder()
                                .message("IDs de dispositivo o cultivo no válidos")
                                .data(null)
                                .build()
                );
            }

            // Procesar las lecturas individuales
            readingDomainService.processDeviceReadings(deviceId, cropId, requestDTO);

            return ResponseEntity.ok(
                    ApiResponseDTO.<String>builder()
                            .message("Lecturas procesadas correctamente")
                            .data("OK")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponseDTO.<String>builder()
                            .message("Error al procesar lecturas: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}