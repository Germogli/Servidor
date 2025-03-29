package com.germogli.backend.education.videos.domain.repository;

import com.germogli.backend.education.videos.domain.model.VideoDomain;

import java.util.List;
import java.util.Optional;

public interface VideoDomainRepository {
    VideoDomain createVideo(VideoDomain videoDomain);
    Optional<VideoDomain> getById(Integer videoId);
    List<VideoDomain> getVideosByModuleId(Integer moduleId);
    VideoDomain updateVideo(VideoDomain videoDomain);
    void deleteVideo(Integer videoId);
}
