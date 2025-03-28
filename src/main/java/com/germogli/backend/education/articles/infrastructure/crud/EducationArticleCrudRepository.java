package com.germogli.backend.education.articles.infrastructure.crud;

import com.germogli.backend.education.articles.infrastructure.entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationArticleCrudRepository extends JpaRepository<ArticleEntity, Integer> {
}
