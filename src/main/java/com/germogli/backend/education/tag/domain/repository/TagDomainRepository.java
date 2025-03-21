package com.germogli.backend.education.tag.domain.repository;

import com.germogli.backend.education.tag.domain.model.TagDomain;

public interface TagDomainRepository {
    TagDomain save(String tagName);
    TagDomain getByName(String tagName);
    void deleteById(Integer tagId);
    void updateTagName(TagDomain tag);
}
