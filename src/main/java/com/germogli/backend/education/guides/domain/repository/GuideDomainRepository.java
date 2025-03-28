package com.germogli.backend.education.guides.domain.repository;

import com.germogli.backend.education.guides.domain.model.GuideDomain;

import java.util.List;
import java.util.Optional;

public interface GuideDomainRepository {
    GuideDomain createGuide(GuideDomain guideDomain);
    List<GuideDomain> getAll();
    List<GuideDomain> getByModuleId(Integer moduleId);
    Optional<GuideDomain> getById(Integer guideId);
    GuideDomain updateGuideInfo(GuideDomain guideDomain);
    void deleteGuide(Integer guideId);
}
