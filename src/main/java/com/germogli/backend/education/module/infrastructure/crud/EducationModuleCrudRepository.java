package com.germogli.backend.education.module.infrastructure.crud;

import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationModuleCrudRepository extends JpaRepository<ModuleEntity, Integer> {
}
