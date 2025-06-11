package com.germogli.backend.common.exception;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador global de excepciones para la aplicación.
 * Intercepta y maneja de forma centralizada los distintos tipos de errores que se puedan presentar.
 */
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("La contraseña ingresada es incorrecta.");
    }

    @ExceptionHandler({AccessDeniedException.class, AdminAccessDeniedException.class})
    public ResponseEntity<String> handleAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tiene permisos para realizar esta acción: " + ex.getMessage());
    }

    // Manejo de errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Manejo de errores de operaciones en base de datos
    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<String> handleDatabaseOperationException(DatabaseOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error en la operación de base de datos: " + ex.getMessage());
    }

    // Captura general para cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // LOG TEMPORAL PARA DEBUG
        System.err.println("🚨 ERROR CAPTURADO: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        ex.printStackTrace();
        // Se recomienda registrar el error completo para fines de depuración.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado. Por favor, intente de nuevo.");
    }

    @ExceptionHandler(CustomForbiddenException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleForbiddenException(CustomForbiddenException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.<String>builder().message(ex.getMessage()).data(null).build(),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<String> handleRoleNotAllowedException(RoleNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleNotificationException(NotificationException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.<String>builder()
                        .message("Error al procesar la notificación: " + ex.getMessage())
                        .data(null)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler(MessageDeliveryException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleMessageDeliveryException(MessageDeliveryException ex) {
        return new ResponseEntity<>(
                ApiResponseDTO.<String>builder()
                        .message("Error al entregar mensaje: " + ex.getMessage())
                        .data(null)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
