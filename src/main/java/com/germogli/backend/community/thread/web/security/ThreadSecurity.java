package com.germogli.backend.community.thread.web.security;

import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.service.ThreadDomainService;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Componente de seguridad para operaciones relacionadas con hilos.
 * Proporciona métodos para verificar si el usuario autenticado tiene permisos
 * para actualizar o eliminar un hilo.
 */
@Component("threadSecurity")
public class ThreadSecurity {

    private final ThreadDomainService threadDomainService;
    private final CommunitySharedService sharedService;

    public ThreadSecurity(ThreadDomainService threadDomainService, CommunitySharedService sharedService) {
        this.threadDomainService = threadDomainService;
        this.sharedService = sharedService;
    }

    /**
     * Verifica si el usuario autenticado puede actualizar el hilo con el ID dado.
     * Solo se permite si el usuario es el creador o tiene rol ADMINISTRADOR.
     *
     * @param threadId  ID del hilo.
     * @param principal Objeto de usuario autenticado.
     * @return true si el usuario puede actualizar el hilo.
     */
    public boolean canUpdate(Integer threadId, UserDetails principal) {
        ThreadDomain thread = threadDomainService.getThreadById(threadId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        return thread.getUserId().equals(currentUser.getId()) ||
                sharedService.hasRole(currentUser, "ADMINISTRADOR");
    }

    /**
     * Verifica si el usuario autenticado puede eliminar el hilo con el ID dado.
     * Solo se permite si el usuario es el creador o tiene rol ADMINISTRADOR.
     *
     * @param threadId  ID del hilo.
     * @param principal Objeto de usuario autenticado.
     * @return true si el usuario puede eliminar el hilo.
     */
    public boolean canDelete(Integer threadId, UserDetails principal) {
        ThreadDomain thread = threadDomainService.getThreadById(threadId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        return thread.getUserId().equals(currentUser.getId()) ||
                sharedService.hasRole(currentUser, "ADMINISTRADOR");
    }
}
