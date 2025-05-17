package com.germogli.backend.monitoring.sensor.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.sensor.application.dto.SensorRequestDTO;
import com.germogli.backend.monitoring.sensor.application.dto.SensorResponseDTO;
import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;
import com.germogli.backend.monitoring.sensor.domain.service.SensorDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de sensores.
 * Proporciona endpoints para crear, obtener, actualizar y eliminar sensores,
 * así como para gestionar la asociación entre cultivos y sensores.
 */
@RestController
@RequestMapping("/sensors")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SensorController {

    private final SensorDomainService sensorDomainService;

    /**
     * Endpoint para crear un nuevo sensor.
     * Cualquier usuario autenticado puede crear sensores.
     *
     * @param request DTO con los datos del sensor.
     * @return Respuesta API con el sensor creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<SensorResponseDTO>> createSensor(@Valid @RequestBody SensorRequestDTO request) {
        SensorDomain sensor = sensorDomainService.createSensor(request);
        return ResponseEntity.ok(ApiResponseDTO.<SensorResponseDTO>builder()
                .message("Sensor creado correctamente")
                .data(sensorDomainService.toResponse(sensor))
                .build());
    }

    /**
     * Endpoint para obtener un sensor por su ID.
     *
     * @param id Identificador del sensor.
     * @return Respuesta API con el sensor encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SensorResponseDTO>> getSensorById(@PathVariable Integer id) {
        SensorDomain sensor = sensorDomainService.getSensorById(id);
        return ResponseEntity.ok(ApiResponseDTO.<SensorResponseDTO>builder()
                .message("Sensor recuperado correctamente")
                .data(sensorDomainService.toResponse(sensor))
                .build());
    }

    /**
     * Endpoint para listar todos los sensores disponibles.
     *
     * @return Respuesta API con la lista de sensores.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<SensorResponseDTO>>> getAllSensors() {
        List<SensorResponseDTO> sensors = sensorDomainService.toResponseList(sensorDomainService.getAllSensors());
        return ResponseEntity.ok(ApiResponseDTO.<List<SensorResponseDTO>>builder()
                .message("Sensores recuperados correctamente")
                .data(sensors)
                .build());
    }

    /**
     * Endpoint para obtener todos los sensores asociados a los cultivos del usuario autenticado.
     *
     * @return Respuesta API con la lista de sensores del usuario.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponseDTO<List<SensorResponseDTO>>> getUserSensors() {
        List<SensorResponseDTO> sensors = sensorDomainService.toResponseList(
                sensorDomainService.getUserSensors());
        return ResponseEntity.ok(ApiResponseDTO.<List<SensorResponseDTO>>builder()
                .message("Sensores del usuario recuperados correctamente")
                .data(sensors)
                .build());
    }

    /**
     * Endpoint para actualizar un sensor.
     * Cualquier usuario autenticado puede actualizar sensores.
     *
     * @param id Identificador del sensor a actualizar.
     * @param request DTO con los nuevos datos.
     * @return Respuesta API con el sensor actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SensorResponseDTO>> updateSensor(
            @PathVariable Integer id, @Valid @RequestBody SensorRequestDTO request) {
        SensorDomain sensor = sensorDomainService.updateSensor(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<SensorResponseDTO>builder()
                .message("Sensor actualizado correctamente")
                .data(sensorDomainService.toResponse(sensor))
                .build());
    }

    /**
     * Endpoint para eliminar un sensor.
     * Cualquier usuario autenticado puede eliminar sensores.
     *
     * @param id Identificador del sensor a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSensor(@PathVariable Integer id) {
        sensorDomainService.deleteSensor(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Sensor eliminado correctamente")
                .build());
    }

    /**
     * Endpoint para obtener los sensores asociados a un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @return Respuesta API con la lista de sensores del cultivo.
     */
    @GetMapping("/crop/{cropId}")
    public ResponseEntity<ApiResponseDTO<List<SensorResponseDTO>>> getSensorsByCropId(@PathVariable Integer cropId) {
        List<SensorResponseDTO> sensors = sensorDomainService.toResponseList(sensorDomainService.getSensorsByCropId(cropId));
        return ResponseEntity.ok(ApiResponseDTO.<List<SensorResponseDTO>>builder()
                .message("Sensores del cultivo recuperados correctamente")
                .data(sensors)
                .build());
    }

    /**
     * Endpoint para asociar un sensor a un cultivo con umbrales personalizados.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Umbral mínimo.
     * @param maxThreshold Umbral máximo.
     * @return Respuesta API confirmando la asociación.
     */
    @PostMapping("/crop/{cropId}/sensor/{sensorId}/thresholds")
    public ResponseEntity<ApiResponseDTO<Void>> addSensorToCropWithThresholds(
            @PathVariable Integer cropId,
            @PathVariable Integer sensorId,
            @RequestParam(value = "minThreshold", required = false) BigDecimal minThreshold,
            @RequestParam(value = "maxThreshold", required = false) BigDecimal maxThreshold) {

        sensorDomainService.addSensorToCropWithThresholds(cropId, sensorId, minThreshold, maxThreshold);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Sensor añadido al cultivo con umbrales correctamente")
                .build());
    }

    /**
     * Endpoint para actualizar los umbrales de un sensor asociado a un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @param minThreshold Nuevo umbral mínimo.
     * @param maxThreshold Nuevo umbral máximo.
     * @return Respuesta API confirmando la actualización.
     */
    @PutMapping("/crop/{cropId}/sensor/{sensorId}/thresholds")
    public ResponseEntity<ApiResponseDTO<Void>> updateSensorThresholds(
            @PathVariable Integer cropId,
            @PathVariable Integer sensorId,
            @RequestParam("minThreshold") BigDecimal minThreshold,
            @RequestParam("maxThreshold") BigDecimal maxThreshold) {

        sensorDomainService.updateSensorThresholds(cropId, sensorId, minThreshold, maxThreshold);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Umbrales del sensor actualizados correctamente")
                .build());
    }

    /**
     * Endpoint para asociar un sensor a un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @return Respuesta API confirmando la asociación.
     */
    @PostMapping("/crop/{cropId}/sensor/{sensorId}")
    public ResponseEntity<ApiResponseDTO<Void>> addSensorToCrop(
            @PathVariable Integer cropId, @PathVariable Integer sensorId) {
        sensorDomainService.addSensorToCrop(cropId, sensorId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Sensor añadido al cultivo correctamente")
                .build());
    }

    /**
     * Endpoint para desasociar un sensor de un cultivo.
     *
     * @param cropId Identificador del cultivo.
     * @param sensorId Identificador del sensor.
     * @return Respuesta API confirmando la desasociación.
     */
    @DeleteMapping("/crop/{cropId}/sensor/{sensorId}")
    public ResponseEntity<ApiResponseDTO<Void>> removeSensorFromCrop(
            @PathVariable Integer cropId, @PathVariable Integer sensorId) {
        sensorDomainService.removeSensorFromCrop(cropId, sensorId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Sensor eliminado del cultivo correctamente")
                .build());
    }
}