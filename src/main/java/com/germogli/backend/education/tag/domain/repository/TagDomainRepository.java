package com.germogli.backend.education.tag.domain.repository;

import com.germogli.backend.education.tag.domain.model.TagDomain;

import java.util.List;

public interface TagDomainRepository {
    TagDomain save(String tagName);
    TagDomain getById(Integer tagId);
    TagDomain getByName(String tagName);
    void deleteById(Integer tagId);
    void updateTagName(TagDomain tag);
    List<TagDomain> findAll();
    Integer getOrCreateTag(String tagName);
}
