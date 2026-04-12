package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.PortalRuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortalRuleRepository extends JpaRepository<PortalRuleEntity, Long>{

	@EntityGraph(attributePaths = {"fromRegion", "toRegion", "requiredTool", "requiredUpgrade"})
	List<PortalRuleEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	@EntityGraph(attributePaths = {"fromRegion", "toRegion", "requiredTool", "requiredUpgrade"})
	Optional<PortalRuleEntity> findByCodeAndActiveTrueAndDeletedAtIsNull(String code);

}
