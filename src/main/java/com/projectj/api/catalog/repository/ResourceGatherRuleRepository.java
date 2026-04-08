package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.ResourceGatherRuleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceGatherRuleRepository extends JpaRepository<ResourceGatherRuleEntity, Long>{

	@EntityGraph(attributePaths = {"region", "resource", "requiredTool"})
	List<ResourceGatherRuleEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	@EntityGraph(attributePaths = {"region", "resource", "requiredTool"})
	@Query("""
		select rule
		from ResourceGatherRuleEntity rule
		where rule.active = true
		  and rule.deletedAt is null
		  and rule.region.code = :regionCode
		  and rule.resource.code = :resourceCode
		""")
	Optional<ResourceGatherRuleEntity> findActiveRule(
		@Param("regionCode") String regionCode,
		@Param("resourceCode") String resourceCode
	);

}
