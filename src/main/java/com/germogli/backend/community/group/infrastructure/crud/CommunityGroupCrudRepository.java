package com.germogli.backend.community.group.infrastructure.crud;

import com.germogli.backend.community.group.infrastructure.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio CRUD básico para GroupEntity.
 * Útil para operaciones simples que no requieran procedimientos almacenados.
 */
public interface CommunityGroupCrudRepository extends JpaRepository<GroupEntity, Integer> {
}
