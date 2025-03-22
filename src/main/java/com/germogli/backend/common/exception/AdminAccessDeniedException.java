package com.germogli.backend.common.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Excepción personalizada para indicar que el usuario no tiene los permisos necesarios
 * para realizar la acción solicitada, por ejemplo, acciones reservadas para administradores.
 */
public class AdminAccessDeniedException extends AccessDeniedException {
    public AdminAccessDeniedException(String msg) {
        super(msg);
    }
}
