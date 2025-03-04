package com.germogli.backend.community.post.domain.repository;



import com.germogli.backend.community.post.domain.model.PostDomain;
import java.util.List;
import java.util.Optional;

public interface PostDomainRepository {
    PostDomain save(PostDomain post);
    Optional<PostDomain> findById(Integer id);
    List<PostDomain> findAll();
    void deleteById(Integer id);
}
