package com.germogli.backend.education.module.domain.repository;

import com.germogli.backend.education.module.domain.model.ModuleDomain;

import java.util.List;

public interface ModuleDomainRepository {
    ModuleDomain createModuleWithTags(ModuleDomain moduleDomain);
    List<ModuleDomain> getAll();
}
