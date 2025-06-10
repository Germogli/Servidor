package com.germogli.backend.monitoring.reading.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.monitoring.domain.service.MonitoringSharedService;
import com.germogli.backend.monitoring.alert.domain.model.AlertDomain;
import com.germogli.backend.monitoring.alert.domain.repository.AlertDomainRepository;
import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.repository.CropDomainRepository;
import com.germogli.backend.monitoring.reading.application.dto.DeviceReadingRequestDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingBatchRequestDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingRequestDTO;
import com.germogli.backend.monitoring.reading.application.dto.ReadingResponseDTO;
import com.germogli.backend.monitoring.reading.domain.model.ReadingDomain;
import com.germogli.backend.monitoring.reading.domain.repository.ReadingDomainRepository;
import com.germogli.backend.monitoring.sensor.domain.model.SensorDomain;
import com.germogli.backend.monitoring.sensor.domain.repository.SensorDomainRepository;
import com.germogli.backend.monitoring.sensor.application.dto.SensorThresholdResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de lecturas de sensores.
 * ACTUALIZADO: Usa exclusivamente umbrales personalizados de la base de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingDomainService {

    private final ReadingDomainRepository readingRepository;
    private final CropDomainRepository cropRepository;
    private final SensorDomainRepository sensorRepository;
    private final AlertDomainRepository alertRepository;
    private final MonitoringSharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea una nueva lectura de sensor.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param request DTO con los datos de la lectura.
     * @return La lectura creada.
     * @throws ResourceNotFoundException si el cultivo o el sensor no existen.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    @Transactional
    public ReadingDomain createReading(ReadingRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + request.getCropId()));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para añadir lecturas a este cultivo");
        }

        // Verificar que el sensor exista
        SensorDomain sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + request.getSensorId()));

        // Crear la lectura
        ReadingDomain reading = ReadingDomain.builder()
                .cropId(request.getCropId())
                .sensorId(request.getSensorId())
                .readingValue(request.getReadingValue())
                .readingDate(request.getReadingDate() != null ? request.getReadingDate() : LocalDateTime.now())
                .build();

        ReadingDomain savedReading = readingRepository.save(reading);

        // Verificar umbrales personalizados y generar alertas si es necesario
        checkPersonalizedThresholdsAndCreateAlert(savedReading, sensor);

        return savedReading;
    }

    /**
     * Procesa un lote de lecturas desde dispositivos IoT.
     * Verifica la autenticación del dispositivo y crea las lecturas en lote.
     *
     * @param request DTO con los datos del lote.
     * @return Lista de lecturas creadas.
     * @throws ResourceNotFoundException si el cultivo o algún sensor no existen.
     */
    @Transactional
    public List<ReadingDomain> processBatchReadings(@Valid ReadingBatchRequestDTO request) {
        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + request.getCropId()));

        // Fecha de las lecturas
        LocalDateTime timestamp = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        // Crear lista de lecturas
        List<ReadingDomain> readings = new ArrayList<>();
        Map<Integer, SensorDomain> sensorCache = new HashMap<>();

        for (ReadingBatchRequestDTO.ReadingSensorDTO sensorReading : request.getReadings()) {
            // Obtener o buscar el sensor (usando cache para evitar múltiples búsquedas)
            SensorDomain sensor = sensorCache.computeIfAbsent(sensorReading.getSensorId(), id ->
                    sensorRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Sensor no encontrado con id: " + id))
            );

            // Crear la lectura
            ReadingDomain reading = ReadingDomain.builder()
                    .cropId(request.getCropId())
                    .sensorId(sensorReading.getSensorId())
                    .readingValue(BigDecimal.valueOf(sensorReading.getValue()))
                    .readingDate(timestamp)
                    .build();

            readings.add(reading);

            // Verificar umbrales personalizados y generar alertas si es necesario
            checkPersonalizedThresholdsAndCreateAlert(reading, sensor);
        }

        // Guardar todas las lecturas en lote
        return readingRepository.saveAll(readings);
    }

    /**
     * ✅ NUEVO: Verifica umbrales personalizados y genera alertas.
     * SOLO usa umbrales configurados en la tabla crop_sensors.
     *
     * @param reading La lectura a verificar.
     * @param sensor El sensor asociado a la lectura.
     */
    private void checkPersonalizedThresholdsAndCreateAlert(ReadingDomain reading, SensorDomain sensor) {
        try {
            // Buscar umbrales personalizados para este cultivo y sensor
            Optional<SensorThresholdResponseDTO> thresholdsOpt =
                    sensorRepository.getThresholdsByCropIdAndSensorId(reading.getCropId(), reading.getSensorId());

            if (thresholdsOpt.isEmpty()) {
                log.debug("No hay umbrales configurados para sensor {} en cultivo {}. No se generará alerta.",
                        reading.getSensorId(), reading.getCropId());
                return;
            }

            SensorThresholdResponseDTO thresholds = thresholdsOpt.get();
            BigDecimal value = reading.getReadingValue();

            // Verificar si el valor está fuera de los umbrales configurados
            boolean isOutOfRange = false;
            String alertLevel = null;
            String alertMessage = null;

            if (thresholds.getMinThreshold() != null && value.compareTo(thresholds.getMinThreshold()) < 0) {
                isOutOfRange = true;
                alertLevel = "high";
                alertMessage = String.format("Valor por debajo del umbral mínimo de %s: %.2f %s (mínimo: %.2f)",
                        sensor.getSensorType(), value, sensor.getUnitOfMeasurement(), thresholds.getMinThreshold());
            } else if (thresholds.getMaxThreshold() != null && value.compareTo(thresholds.getMaxThreshold()) > 0) {
                isOutOfRange = true;
                alertLevel = "high";
                alertMessage = String.format("Valor por encima del umbral máximo de %s: %.2f %s (máximo: %.2f)",
                        sensor.getSensorType(), value, sensor.getUnitOfMeasurement(), thresholds.getMaxThreshold());
            }

            // Si se detecta un valor fuera de rango, generar alerta
            if (isOutOfRange) {
                log.info("Generando alerta para lectura fuera de umbrales: {}", alertMessage);

                // Crear alerta usando el procedimiento almacenado
                AlertDomain alert = alertRepository.processAlert(
                        reading.getCropId(),
                        reading.getSensorId(),
                        alertLevel,
                        alertMessage
                );

                // Enviar notificación al propietario del cultivo
                CropDomain crop = cropRepository.findById(reading.getCropId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + reading.getCropId()));

                notificationService.sendNotification(
                        crop.getUserId(),
                        alertMessage,
                        "sensor_alert"
                );

                log.info("Alerta generada exitosamente con ID: {} para cultivo: {}",
                        alert.getId(), reading.getCropId());
            } else {
                log.debug("Lectura dentro de umbrales normales: {} = {} {}",
                        sensor.getSensorType(), value, sensor.getUnitOfMeasurement());
            }

        } catch (Exception e) {
            log.error("Error al verificar umbrales personalizados para lectura: cropId={}, sensorId={}, error={}",
                    reading.getCropId(), reading.getSensorId(), e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo de creación de lecturas
        }
    }

    /**
     * Procesa lecturas provenientes directamente de un dispositivo ESP32.
     * Identifica los sensores correspondientes por tipo y registra las lecturas.
     *
     * @param deviceId ID del dispositivo (para registro)
     * @param cropId ID del cultivo al que pertenecen las lecturas
     * @param requestDTO DTO con los datos de temperatura, humedad y TDS
     * @return Lista de lecturas procesadas
     */
    @Transactional
    public List<ReadingDomain> processDeviceReadings(Integer deviceId, Integer cropId, DeviceReadingRequestDTO requestDTO) {
        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        List<ReadingDomain> readings = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now();

        // Mapear los sensores por tipo (asumiendo que ya existen en la BD)
        Map<String, SensorDomain> sensorsByType = getSensorsByTypeForCrop(cropId);

        // Procesar temperatura si hay datos y existe el sensor
        if (requestDTO.getTemperature() != null && sensorsByType.containsKey("temperature")) {
            SensorDomain tempSensor = sensorsByType.get("temperature");
            ReadingDomain reading = ReadingDomain.builder()
                    .cropId(cropId)
                    .sensorId(tempSensor.getId())
                    .readingValue(requestDTO.getTemperature())
                    .readingDate(timestamp)
                    .build();

            readings.add(readingRepository.save(reading));
            checkPersonalizedThresholdsAndCreateAlert(reading, tempSensor);
        }

        // Procesar humedad si hay datos y existe el sensor
        if (requestDTO.getHumedad() != null && sensorsByType.containsKey("humidity")) {
            SensorDomain humiditySensor = sensorsByType.get("humidity");
            ReadingDomain reading = ReadingDomain.builder()
                    .cropId(cropId)
                    .sensorId(humiditySensor.getId())
                    .readingValue(requestDTO.getHumedad())
                    .readingDate(timestamp)
                    .build();

            readings.add(readingRepository.save(reading));
            checkPersonalizedThresholdsAndCreateAlert(reading, humiditySensor);
        }

        // Procesar TDS si hay datos y existe el sensor
        if (requestDTO.getTds() != null && sensorsByType.containsKey("tds")) {
            SensorDomain tdsSensor = sensorsByType.get("tds");
            ReadingDomain reading = ReadingDomain.builder()
                    .cropId(cropId)
                    .sensorId(tdsSensor.getId())
                    .readingValue(requestDTO.getTds())
                    .readingDate(timestamp)
                    .build();

            readings.add(readingRepository.save(reading));
            checkPersonalizedThresholdsAndCreateAlert(reading, tdsSensor);
        }

        return readings;
    }

    /**
     * Obtiene una lectura por su ID.
     * Verifica que el usuario tenga acceso al cultivo asociado a la lectura.
     *
     * @param id ID de la lectura.
     * @return La lectura encontrada.
     * @throws ResourceNotFoundException si la lectura no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public ReadingDomain getReadingById(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        ReadingDomain reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura no encontrada con id: " + id));

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(reading.getCropId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + reading.getCropId()));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a esta lectura");
        }

        return reading;
    }

    /**
     * Obtiene todas las lecturas de un cultivo específico.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @return Lista de lecturas del cultivo.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public List<ReadingDomain> getReadingsByCropId(Integer cropId) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a las lecturas de este cultivo");
        }

        return readingRepository.findByCropId(cropId);
    }

    /**
     * Obtiene el historial de lecturas de un sensor específico en un cultivo,
     * dentro de un rango de fechas.
     * Verifica que el usuario tenga acceso al cultivo.
     *
     * @param cropId ID del cultivo.
     * @param sensorId ID del sensor.
     * @param startDate Fecha de inicio del rango (opcional).
     * @param endDate Fecha de fin del rango (opcional).
     * @param limit Límite de registros a recuperar (opcional).
     * @return Lista de lecturas que cumplen con los criterios.
     * @throws ResourceNotFoundException si el cultivo no existe.
     * @throws AccessDeniedException si el usuario no tiene acceso al cultivo.
     */
    public List<ReadingDomain> getReadingHistory(Integer cropId, Integer sensorId,
                                                 LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        // Verificar que el cultivo exista
        CropDomain crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado con id: " + cropId));

        // Verificar que el usuario actual sea el propietario o un administrador
        boolean isOwner = crop.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder al historial de lecturas de este cultivo");
        }

        // Establecer valores por defecto si no se proporcionan
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusDays(7);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();
        int effectiveLimit = limit != null ? limit : 100;

        return readingRepository.findByCropIdAndSensorIdAndDateRange(
                cropId, sensorId, effectiveStartDate, effectiveEndDate, effectiveLimit);
    }

    /**
     * Método auxiliar para obtener los sensores asociados a un cultivo, mapeados por tipo.
     */
    private Map<String, SensorDomain> getSensorsByTypeForCrop(Integer cropId) {
        List<SensorDomain> sensors = sensorRepository.findByCropId(cropId);
        Map<String, SensorDomain> sensorsByType = new HashMap<>();

        for (SensorDomain sensor : sensors) {
            sensorsByType.put(sensor.getSensorType().toLowerCase(), sensor);
        }

        return sensorsByType;
    }

    /**
     * Convierte un objeto ReadingDomain en un DTO de respuesta.
     * Incluye información adicional del sensor.
     *
     * @param reading Lectura a convertir.
     * @return DTO con la información de la lectura.
     */
    public ReadingResponseDTO toResponse(ReadingDomain reading) {
        // Obtener información adicional del sensor
        SensorDomain sensor = sensorRepository.findById(reading.getSensorId())
                .orElse(null);

        return ReadingResponseDTO.builder()
                .id(reading.getId())
                .cropId(reading.getCropId())
                .sensorId(reading.getSensorId())
                .readingValue(reading.getReadingValue())
                .readingDate(reading.getReadingDate())
                .sensorType(sensor != null ? sensor.getSensorType() : null)
                .unitOfMeasurement(sensor != null ? sensor.getUnitOfMeasurement() : null)
                .build();
    }

    /**
     * Convierte una lista de ReadingDomain en una lista de DTOs de respuesta.
     *
     * @param readings Lista de lecturas.
     * @return Lista de DTOs.
     */
    public List<ReadingResponseDTO> toResponseList(List<ReadingDomain> readings) {
        return readings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}