package com.germogli.backend.education.module.domain.repository;

import com.germogli.backend.education.module.domain.model.ModuleDomain;

public interface ModuleDomainRepository {
    ModuleDomain createModuleWithTags(ModuleDomain moduleDomain);
}
