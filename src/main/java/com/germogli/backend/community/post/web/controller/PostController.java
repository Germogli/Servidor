package com.germogli.backend.community.post.web.controller;

import com.germogli.backend.community.post.application.dto.ApiResponseDTO;
import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.service.PostDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostDomainService postDomainService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> createPost(@Valid @RequestBody CreatePostRequestDTO request) {
        PostDomain createdPost = postDomainService.createPost(request);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("La publicación se hizo correctamente")
                .data(postDomainService.toResponse(createdPost))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> getPostById(@PathVariable Integer id) {
        PostDomain post = postDomainService.getPostById(id);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Post recuperado correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PostResponseDTO>>> getAllPosts() {
        List<PostResponseDTO> responseList = postDomainService.toResponseList(postDomainService.getAllPosts());
        return ResponseEntity.ok(ApiResponseDTO.<List<PostResponseDTO>>builder()
                .message("Posts recuperados correctamente")
                .data(responseList)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> updatePost(@PathVariable Integer id,
                                                                      @Valid @RequestBody UpdatePostRequestDTO request) {
        PostDomain updatedPost = postDomainService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("La publicación se actualizó correctamente")
                .data(postDomainService.toResponse(updatedPost))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deletePost(@PathVariable Integer id) {
        postDomainService.deletePost(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("La publicación se eliminó correctamente")
                .build());
    }
}