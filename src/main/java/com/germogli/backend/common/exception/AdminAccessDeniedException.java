package com.germogli.backend.common.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Excepción personalizada para indicar que un usuario no tiene los permisos necesarios,
 * por ejemplo, para realizar acciones reservadas a administradores.
 */
public class AdminAccessDeniedException extends AccessDeniedException {
    public AdminAccessDeniedException(String msg) {
        super(msg);
    }
}
