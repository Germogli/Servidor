package com.germogli.backend.education.videos.infrastructure.crud;

import com.germogli.backend.education.videos.infrastructure.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationVideosCrudRepository extends JpaRepository<VideoEntity, Integer> {
}
