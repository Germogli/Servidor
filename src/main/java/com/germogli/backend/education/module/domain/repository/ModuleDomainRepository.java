package com.germogli.backend.education.module.domain.repository;

import com.germogli.backend.education.module.domain.model.ModuleDomain;

import java.util.List;
import java.util.Optional;

public interface ModuleDomainRepository {
    ModuleDomain createModuleWithTags(ModuleDomain moduleDomain);
    List<ModuleDomain> getAll();
    ModuleDomain updateModuleWithTags(ModuleDomain moduleDomain);
    Optional<ModuleDomain> getById(Integer moduleId);
    List<ModuleDomain> filterModulesByTags(List<Integer> tagIds);
    void deleteModule(Integer moduleId);
}
