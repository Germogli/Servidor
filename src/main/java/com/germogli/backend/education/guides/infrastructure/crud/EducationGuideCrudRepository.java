package com.germogli.backend.education.guides.infrastructure.crud;

import com.germogli.backend.education.guides.infrastructure.entity.GuideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationGuideCrudRepository extends JpaRepository<GuideEntity, Integer> {
}
