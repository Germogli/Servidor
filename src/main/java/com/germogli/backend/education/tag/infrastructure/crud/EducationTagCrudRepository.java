package com.germogli.backend.education.tag.infrastructure.crud;

import com.germogli.backend.education.tag.infrastructure.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationTagCrudRepository extends JpaRepository<TagEntity, Integer> {
}
