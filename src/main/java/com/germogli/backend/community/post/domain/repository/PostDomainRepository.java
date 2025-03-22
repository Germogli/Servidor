package com.germogli.backend.community.post.domain.repository;

import com.germogli.backend.community.post.domain.model.PostDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Post.
 */
public interface PostDomainRepository {
    PostDomain save(PostDomain post);
    Optional<PostDomain> findById(Integer id);
    List<PostDomain> findAll();
    void deleteById(Integer id);
}
