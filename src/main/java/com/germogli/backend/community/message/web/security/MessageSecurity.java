package com.germogli.backend.community.message.web.security;

import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.service.MessageDomainService;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Componente de seguridad para operaciones relacionadas con mensajes.
 * Proporciona métodos para verificar si el usuario autenticado tiene permisos
 * para eliminar un mensaje.
 */
@Component("messageSecurity")
    public class MessageSecurity {

    private final MessageDomainService messageDomainService;
    private final CommunitySharedService sharedService;

    public MessageSecurity(MessageDomainService messageDomainService, CommunitySharedService sharedService) {
        this.messageDomainService = messageDomainService;
        this.sharedService = sharedService;
    }

    /**
     * Verifica si el usuario autenticado puede eliminar el mensaje con el ID dado.
     * Se permite si el usuario es el propietario, o si tiene rol ADMINISTRADOR o MODERADOR.
     *
     * @param messageId ID del mensaje.
     * @param principal Objeto de usuario autenticado.
     * @return true si el usuario puede eliminar el mensaje.
     */
    public boolean canDelete(Integer messageId, UserDetails principal) {
        MessageDomain message = messageDomainService.getMessageById(messageId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        return message.getUserId().equals(currentUser.getId()) ||
                sharedService.hasRole(currentUser, "ADMINISTRADOR") ||
                sharedService.hasRole(currentUser, "MODERADOR");
    }
}
