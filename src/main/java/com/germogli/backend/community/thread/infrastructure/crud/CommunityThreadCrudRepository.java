package com.germogli.backend.community.thread.infrastructure.crud;

import com.germogli.backend.community.thread.infrastructure.entity.ThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio CRUD básico para ThreadEntity.
 * Útil para operaciones simples que no requieran procedimientos almacenados.
 */
public interface CommunityThreadCrudRepository extends JpaRepository<ThreadEntity, Integer> {
}
