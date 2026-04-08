package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToolRepository extends JpaRepository<ToolEntity, Long>{

	List<ToolEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	List<ToolEntity> findByDefaultUnlockedTrueAndActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	Optional<ToolEntity> findByCodeAndActiveTrueAndDeletedAtIsNull(String code);

}
