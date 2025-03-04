package com.germogli.backend.community.post.infrastructure.repository;

import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.community.post.infrastructure.crud.PostCrudRepository;
import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostRepository implements PostDomainRepository {
    private final PostCrudRepository postCrudRepository;

    @Override
    public PostDomain save(PostDomain post) {
        // Si es un nuevo post, asigna la fecha actual
        if (post.getId() == null) {
            post.setPostDate(LocalDateTime.now());
        }
        PostEntity entity = post.toEntity();
        PostEntity savedEntity = postCrudRepository.save(entity);
        return PostDomain.fromEntity(savedEntity);
    }

    @Override
    public Optional<PostDomain> findById(Integer id) {
        return postCrudRepository.findById(id)
                .map(PostDomain::fromEntity);
    }

    @Override
    public List<PostDomain> findAll() {
        return postCrudRepository.findAll()
                .stream()
                .map(PostDomain::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        postCrudRepository.deleteById(id);
    }
}
