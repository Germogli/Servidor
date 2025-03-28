package com.germogli.backend.education.articles.domain.repository;

import com.germogli.backend.education.articles.domain.model.ArticleDomain;

public interface ArticleDomainRepository {
    ArticleDomain createArticle(ArticleDomain articleDomain);
}
