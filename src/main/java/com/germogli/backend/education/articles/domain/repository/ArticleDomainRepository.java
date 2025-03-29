package com.germogli.backend.education.articles.domain.repository;

import com.germogli.backend.education.articles.domain.model.ArticleDomain;

import java.util.List;
import java.util.Optional;

public interface ArticleDomainRepository {
    ArticleDomain createArticle(ArticleDomain articleDomain);
    List<ArticleDomain> getByArticlesByModuleId(Integer id);
    Optional<ArticleDomain> getById(Integer id);
    ArticleDomain updateArticleInfo(ArticleDomain articleDomain);
}
