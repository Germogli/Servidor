package com.germogli.backend.community.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.community.group.domain.repository.GroupDomainRepository;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servicio compartido para operaciones comunes dentro del módulo Community.
 * Proporciona utilidades para obtener el usuario autenticado, validar roles y verificar recursos relacionados.
 */
@Service
@RequiredArgsConstructor
public class CommunitySharedService {

    private final UserDomainRepository userRepository;
    private final PostDomainRepository postRepository;
    private final GroupDomainRepository groupRepository;

    /**
     * Obtiene el usuario autenticado actual desde el contexto de seguridad.
     *
     * @return Usuario autenticado como entidad de dominio.
     * @throws ResourceNotFoundException si el usuario no se encuentra en el sistema.
     */
    public UserDomain getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    /**
     * Verifica si un usuario posee un rol específico.
     *
     * @param userDomain Usuario a verificar.
     * @param role       Rol esperado (por ejemplo, "ADMINISTRADOR").
     * @return true si el usuario posee el rol indicado, false en caso contrario.
     */
    public boolean hasRole(UserDomain userDomain, String role) {
        return userDomain.getRole() != null &&
                userDomain.getRole().getRoleType().equalsIgnoreCase(role);
    }

    /**
     * Valida que una publicación exista en el sistema.
     *
     * @param postId Identificador de la publicación.
     * @throws ResourceNotFoundException si la publicación no existe.
     */
    public void validatePostExists(Integer postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con id: " + postId));
    }

    /**
     * Obtiene el ID del propietario de una publicación.
     *
     * @param postId Identificador de la publicación.
     * @return ID del usuario propietario de la publicación.
     * @throws ResourceNotFoundException si la publicación no existe.
     */
    public Integer getOwnerIdOfPost(Integer postId) {
        return postRepository.findOwnerIdByPostId(postId);
    }

    /**
     * Valida que un grupo exista en el sistema.
     * @param groupId Identificador del grupo
     * @throws ResourceNotFoundException si el grupo no existe
     */
    public void validateGroupExists(Integer groupId) {
        if (groupId != null && !groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Grupo no encontrado con id: " + groupId);
        }
    }
    /**
     * Verifica si un usuario pertenece a un grupo.
     *
     * @param userId ID del usuario
     * @param groupId ID del grupo
     * @return true si el usuario pertenece al grupo, false en caso contrario
     */
    public boolean isUserInGroup(Integer userId, Integer groupId) {
        // Esta lógica dependerá de la implementación existente
        // Podría usar userGroupCrudRepository para verificar
        return groupRepository.isUserInGroup(userId, groupId);
    }


}
