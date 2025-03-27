package com.germogli.backend.education.guides.domain.repository;

import com.germogli.backend.education.guides.domain.model.GuideDomain;

import java.util.List;

public interface GuideDomainRepository {
    GuideDomain createGuide(GuideDomain guideDomain);
    List<GuideDomain> getAll();
}
