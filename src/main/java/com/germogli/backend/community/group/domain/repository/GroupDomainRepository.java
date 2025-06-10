package com.germogli.backend.community.group.domain.repository;

import com.germogli.backend.community.group.domain.model.GroupDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Group.
 */
public interface GroupDomainRepository {
    GroupDomain save(GroupDomain group);
    Optional<GroupDomain> findById(Integer id);
    List<GroupDomain> findAll();
    void deleteById(Integer id);
    boolean existsById(Integer id);
    List<GroupDomain> findGroupsByUserId(Integer userId);
    boolean isUserInGroup(Integer userId, Integer groupId);
    void leaveGroup(Integer userId, Integer groupId);
}
