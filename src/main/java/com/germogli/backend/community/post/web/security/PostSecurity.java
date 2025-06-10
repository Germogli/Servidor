package com.germogli.backend.community.post.web.security;

import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.service.PostDomainService;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Componente de seguridad para operaciones relacionadas con publicaciones.
 * Proporciona métodos para verificar si el usuario autenticado tiene permisos
 * para actualizar o eliminar un post.
 */
@Component("postSecurity")
public class PostSecurity {

    private final PostDomainService postDomainService;
    private final CommunitySharedService sharedService;

    public PostSecurity(PostDomainService postDomainService, CommunitySharedService sharedService) {
        this.postDomainService = postDomainService;
        this.sharedService = sharedService;
    }

    /**
     * Verifica si el usuario autenticado puede actualizar el post con el ID dado.
     * Se permite si el usuario es el propietario o tiene rol ADMINISTRADOR.
     *
     * @param postId    ID del post.
     * @param principal Objeto de usuario autenticado.
     * @return true si el usuario puede actualizar el post.
     */
    public boolean canUpdate(Integer postId, UserDetails principal) {
        PostDomain post = postDomainService.getPostById(postId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        return post.getUserId().equals(currentUser.getId()) ||
                sharedService.hasRole(currentUser, "ADMINISTRADOR");
    }

    /**
     * Verifica si el usuario autenticado puede eliminar el post con el ID dado.
     * Se permite si el usuario es el propietario o tiene rol ADMINISTRADOR.
     *
     * @param postId    ID del post.
     * @param principal Objeto de usuario autenticado.
     * @return true si el usuario puede eliminar el post.
     */
    public boolean canDelete(Integer postId, UserDetails principal) {
        PostDomain post = postDomainService.getPostById(postId);
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        return post.getUserId().equals(currentUser.getId()) ||
                sharedService.hasRole(currentUser, "ADMINISTRADOR");
    }
}
