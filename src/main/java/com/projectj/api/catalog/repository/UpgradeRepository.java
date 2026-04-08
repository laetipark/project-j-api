package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.UpgradeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpgradeRepository extends JpaRepository<UpgradeEntity, Long>{

	@EntityGraph(attributePaths = {"tool", "prerequisiteUpgrade"})
	List<UpgradeEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	@EntityGraph(attributePaths = {"tool", "prerequisiteUpgrade"})
	Optional<UpgradeEntity> findByCodeAndActiveTrueAndDeletedAtIsNull(String code);

}
