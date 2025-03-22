package com.germogli.backend.community.thread.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.thread.application.dto.CreateThreadRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadResponseDTO;
import com.germogli.backend.community.thread.application.dto.CreateThreadReplyRequestDTO;
import com.germogli.backend.community.thread.application.dto.ThreadReplyResponseDTO;
import com.germogli.backend.community.thread.domain.service.ThreadDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de hilos y respuestas en Community.
 */
@RestController
@RequestMapping("/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreadDomainService threadDomainService;

    // Endpoints para hilos

    /**
     * Crea un nuevo hilo.
     *
     * @param request DTO con los datos del nuevo hilo.
     * @return Respuesta API con el hilo creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> createThread(@Valid @RequestBody CreateThreadRequestDTO request) {
        var thread = threadDomainService.createThread(request);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Thread creado correctamente")
                .data(threadDomainService.toThreadResponse(thread))
                .build());
    }

    /**
     * Obtiene un hilo por su ID.
     *
     * @param id Identificador del hilo.
     * @return Respuesta API con el hilo encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> getThreadById(@PathVariable Integer id) {
        var thread = threadDomainService.getThreadById(id);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Thread recuperado correctamente")
                .data(threadDomainService.toThreadResponse(thread))
                .build());
    }

    /**
     * Obtiene todos los hilos.
     *
     * @return Respuesta API con la lista de hilos.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ThreadResponseDTO>>> getAllThreads() {
        var threads = threadDomainService.getAllThreads();
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadResponseDTO>>builder()
                .message("Threads recuperados correctamente")
                .data(threadDomainService.toThreadResponseList(threads))
                .build());
    }

    /**
     * Actualiza un hilo.
     *
     * @param id      Identificador del hilo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return Respuesta API con el hilo actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ThreadResponseDTO>> updateThread(@PathVariable Integer id,
                                                                          @Valid @RequestBody CreateThreadRequestDTO request) {
        var thread = threadDomainService.updateThread(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadResponseDTO>builder()
                .message("Thread actualizado correctamente")
                .data(threadDomainService.toThreadResponse(thread))
                .build());
    }

    /**
     * Elimina un hilo.
     *
     * @param id Identificador del hilo a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteThread(@PathVariable Integer id) {
        threadDomainService.deleteThread(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Thread eliminado correctamente")
                .build());
    }

    // Endpoints para respuestas (thread replies)

    /**
     * Crea una respuesta para un hilo.
     *
     * @param threadId Identificador del hilo al que se agrega la respuesta.
     * @param request  DTO con los datos de la respuesta.
     * @return Respuesta API con la respuesta creada.
     */
    @PostMapping("/{threadId}/replies")
    public ResponseEntity<ApiResponseDTO<ThreadReplyResponseDTO>> createThreadReply(@PathVariable Integer threadId,
                                                                                    @Valid @RequestBody CreateThreadReplyRequestDTO request) {
        // Asigna el threadId del path al DTO.
        request.setThreadId(threadId);
        var reply = threadDomainService.createThreadReply(request);
        return ResponseEntity.ok(ApiResponseDTO.<ThreadReplyResponseDTO>builder()
                .message("Respuesta creada correctamente")
                .data(threadDomainService.toThreadReplyResponse(reply))
                .build());
    }

    /**
     * Obtiene todas las respuestas de un hilo.
     *
     * @param threadId Identificador del hilo.
     * @return Respuesta API con la lista de respuestas.
     */
    @GetMapping("/{threadId}/replies")
    public ResponseEntity<ApiResponseDTO<List<ThreadReplyResponseDTO>>> getRepliesByThread(@PathVariable Integer threadId) {
        var replies = threadDomainService.getRepliesByThreadId(threadId);
        return ResponseEntity.ok(ApiResponseDTO.<List<ThreadReplyResponseDTO>>builder()
                .message("Respuestas recuperadas correctamente")
                .data(threadDomainService.toThreadReplyResponseList(replies))
                .build());
    }

    /**
     * Elimina una respuesta de un hilo.
     *
     * @param replyId Identificador de la respuesta a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteThreadReply(@PathVariable Integer replyId) {
        threadDomainService.deleteThreadReply(replyId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Respuesta eliminada correctamente")
                .build());
    }
}
