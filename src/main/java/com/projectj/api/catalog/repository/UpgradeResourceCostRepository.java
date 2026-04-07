package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UpgradeResourceCostRepository extends JpaRepository<UpgradeResourceCostEntity, Long>{

	@EntityGraph(attributePaths = {"resource"})
	List<UpgradeResourceCostEntity> findByUpgradeIdOrderByIdAsc(Long upgradeId);

}
