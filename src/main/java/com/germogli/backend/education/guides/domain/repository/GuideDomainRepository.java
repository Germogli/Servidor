package com.germogli.backend.education.guides.domain.repository;

import com.germogli.backend.education.guides.domain.model.GuideDomain;

public interface GuideDomainRepository {
    GuideDomain createGuide(GuideDomain guideDomain);
}
